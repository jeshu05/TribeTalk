import sqlite3
import os
import sys

def validate_db():
    db_path = os.path.join("app", "src", "main", "assets", "database", "nipun.db")
    if not os.path.exists(db_path):
        print(f"Error: Database file not found at {db_path}")
        sys.exit(1)

    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    required_tables = [
        "teachers", "classrooms", "domains", "competencies",
        "learning_outcomes", "lessons", "flashcards", "worksheets",
        "students", "student_outcome_progress", "teaching_resources",
        "content_translations"
    ]

    cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = [row[0] for row in cursor.fetchall()]

    for t in required_tables:
        if t not in tables:
            print(f"Error: Required table '{t}' missing from nipun.db")
            sys.exit(1)

    cursor.execute("SELECT count(*) FROM learning_outcomes;")
    lo_count = cursor.fetchone()[0]
    print(f"Validation SUCCESS: nipun.db exists, contains {len(tables)} tables, and {lo_count} learning outcomes.")
    conn.close()

if __name__ == "__main__":
    validate_db()
