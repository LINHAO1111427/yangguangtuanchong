#!/usr/bin/env python3
import argparse
import datetime as dt
import json
import mimetypes
import os
import re
import sys
import time
import uuid
from urllib.parse import urlparse

try:
    from minio import Minio
    from minio.error import S3Error
except ImportError:
    Minio = None
    S3Error = Exception

try:
    import psycopg2
    from psycopg2.extras import Json
except ImportError:
    psycopg2 = None
    Json = None


ALLOWED_MIME = {
    ".mp4": "video/mp4",
    ".mov": "video/quicktime",
    ".mkv": "video/x-matroska",
    ".mpeg": "video/mpeg",
    ".mpg": "video/mpeg",
    ".webm": "video/webm",
}


def parse_args():
    parser = argparse.ArgumentParser(
        description="Upload local videos to MinIO and insert rows into content_post."
    )
    parser.add_argument(
        "--source-dir",
        default=r"C:\WorkSpace\xiaolvshu\新建文件夹",
        help="Folder containing videos.",
    )
    parser.add_argument("--user-id", type=int, default=1, help="content_post.user_id")
    parser.add_argument("--topic-id", type=int, default=None, help="content_post.publish_topic_id")
    parser.add_argument("--tags", default="", help="Comma-separated tag list.")
    parser.add_argument("--title-template", default="{stem}", help="Title template.")
    parser.add_argument(
        "--content-text",
        default="Imported video.",
        help="Content text for the post.",
    )
    parser.add_argument("--status", type=int, default=1, help="0=draft,1=published")
    parser.add_argument("--is-public", type=int, default=1, help="0=private,1=public")
    parser.add_argument("--allow-comment", type=int, default=1, help="0=no,1=yes")
    parser.add_argument("--directory", default="content/video", help="MinIO directory prefix")
    parser.add_argument("--dry-run", action="store_true", help="Skip upload and DB insert")
    parser.add_argument("--recursive", action="store_true", help="Scan subfolders")
    return parser.parse_args()


def require_dependency(dep, name):
    if dep is None:
        print(f"Missing dependency: {name}. Install it first.", file=sys.stderr)
        print("pip install minio psycopg2-binary", file=sys.stderr)
        sys.exit(1)


def parse_endpoint(endpoint):
    if "://" in endpoint:
        parsed = urlparse(endpoint)
        secure = parsed.scheme == "https"
        host = parsed.netloc
        public_base = f"{parsed.scheme}://{parsed.netloc}"
    else:
        secure = False
        host = endpoint
        public_base = f"http://{endpoint}"
    return host, secure, public_base.rstrip("/")


def build_object_path(directory, filename):
    date_prefix = dt.datetime.now().strftime("%Y%m%d")
    stem, ext = os.path.splitext(filename)
    safe_stem = re.sub(r"[^\w.-]+", "_", stem, flags=re.UNICODE).strip("_") or "video"
    suffix = f"{int(time.time() * 1000)}_{uuid.uuid4().hex[:6]}"
    new_name = f"{safe_stem}_{suffix}{ext}"
    return f"{directory}/{date_prefix}/{new_name}"


def pick_mime(file_path):
    ext = os.path.splitext(file_path)[1].lower()
    if ext in ALLOWED_MIME:
        return ALLOWED_MIME[ext], ext
    guessed, _ = mimetypes.guess_type(file_path)
    return guessed, ext


def iter_files(source_dir, recursive):
    if recursive:
        for root, _, files in os.walk(source_dir):
            for name in files:
                yield os.path.join(root, name)
    else:
        for name in os.listdir(source_dir):
            full = os.path.join(source_dir, name)
            if os.path.isfile(full):
                yield full


def main():
    args = parse_args()
    require_dependency(Minio, "minio")
    require_dependency(psycopg2, "psycopg2")

    minio_endpoint = os.getenv("MINIO_ENDPOINT", "http://localhost:9000")
    minio_access_key = os.getenv("MINIO_ACCESS_KEY", "minioadmin")
    minio_secret_key = os.getenv("MINIO_SECRET_KEY", "minioadmin")
    minio_bucket = os.getenv("MINIO_BUCKET", "xiaolvshu-dev")
    minio_public_url = os.getenv("MINIO_PUBLIC_URL")

    host, secure, public_base = parse_endpoint(minio_endpoint)
    client = Minio(host, access_key=minio_access_key, secret_key=minio_secret_key, secure=secure)

    public_base = minio_public_url.rstrip("/") if minio_public_url else public_base
    public_bucket_base = f"{public_base}/{minio_bucket}"

    db_host = os.getenv("DB_HOST", "127.0.0.1")
    db_port = int(os.getenv("DB_PORT", "55432"))
    db_name = os.getenv("DB_NAME", "xiaolvshu_content")
    db_user = os.getenv("DB_USER", "postgres")
    db_password = os.getenv("DB_PASSWORD", "postgres")

    if not args.dry_run and not client.bucket_exists(minio_bucket):
        client.make_bucket(minio_bucket)

    tags = [t.strip() for t in args.tags.split(",") if t.strip()] if args.tags else None

    conn = psycopg2.connect(
        host=db_host,
        port=db_port,
        dbname=db_name,
        user=db_user,
        password=db_password,
    )
    conn.autocommit = False

    inserted = 0
    skipped = 0

    for file_path in iter_files(args.source_dir, args.recursive):
        mime, ext = pick_mime(file_path)
        if mime not in ALLOWED_MIME.values():
            skipped += 1
            continue

        file_name = os.path.basename(file_path)
        object_path = build_object_path(args.directory, file_name)
        video_url = f"{public_bucket_base}/{object_path}"
        video_format = ext.lstrip(".") if ext else None
        file_size = os.path.getsize(file_path)
        title = args.title_template.format(stem=os.path.splitext(file_name)[0])
        now = dt.datetime.now()

        if not args.dry_run:
            try:
                client.fput_object(
                    minio_bucket,
                    object_path,
                    file_path,
                    content_type=mime,
                )
            except S3Error as exc:
                print(f"Upload failed: {file_path} -> {exc}", file=sys.stderr)
                continue

        sql = """
            INSERT INTO content_post (
                user_id,
                content_type,
                title,
                content,
                publish_topic_id,
                video_url,
                video_format,
                video_file_size,
                is_public,
                status,
                allow_comment,
                audit_status,
                view_count,
                like_count,
                comment_count,
                share_count,
                collect_count,
                forward_count,
                hot_score,
                recommend_score,
                create_time,
                update_time,
                publish_time,
                tags
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,
                0, 0, 0, 0, 0, 0, 0, 0,
                %s, %s, %s, %s
            )
            RETURNING id
        """
        params = (
            args.user_id,
            2,
            title,
            args.content_text,
            args.topic_id,
            video_url,
            video_format,
            file_size,
            args.is_public,
            args.status,
            args.allow_comment,
            0,
            now,
            now,
            now if args.status == 1 else None,
            Json(tags) if tags is not None else None,
        )

        if args.dry_run:
            print(f"[DRY RUN] {file_path} -> {video_url}")
            continue

        try:
            with conn.cursor() as cur:
                cur.execute(sql, params)
                content_id = cur.fetchone()[0]
            conn.commit()
        except Exception as exc:
            conn.rollback()
            try:
                client.remove_object(minio_bucket, object_path)
            except Exception:
                pass
            print(f"DB insert failed: {file_path} -> {exc}", file=sys.stderr)
            continue

        inserted += 1
        print(f"Inserted content_post id={content_id} url={video_url}")

    conn.close()
    print(f"Done. Inserted={inserted}, Skipped={skipped}")


if __name__ == "__main__":
    main()
