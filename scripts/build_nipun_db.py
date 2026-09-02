import json
import sqlite3
import os

def build_db():
    json_path = os.path.join("data", "curriculum", "demo", "curriculum_demo.json")
    out_dir = os.path.join("app", "src", "main", "assets", "database")
    os.makedirs(out_dir, exist_ok=True)
    db_path = os.path.join(out_dir, "nipun.db")

    if os.path.exists(db_path):
        os.remove(db_path)

    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    # Create Room Compatible Tables
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS teachers (
        id TEXT PRIMARY KEY NOT NULL,
        name TEXT NOT NULL,
        selectedClassesJson TEXT NOT NULL,
        createdAt INTEGER NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS classrooms (
        classId TEXT PRIMARY KEY NOT NULL,
        grade TEXT NOT NULL,
        name TEXT NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS domains (
        domainId TEXT PRIMARY KEY NOT NULL,
        name TEXT NOT NULL,
        type TEXT NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS competencies (
        competencyId TEXT PRIMARY KEY NOT NULL,
        domainId TEXT NOT NULL,
        code TEXT NOT NULL,
        descriptionHindi TEXT NOT NULL,
        descriptionSantali TEXT NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS learning_outcomes (
        outcomeId TEXT PRIMARY KEY NOT NULL,
        competencyId TEXT NOT NULL,
        nipunCode TEXT NOT NULL,
        grade TEXT NOT NULL,
        domain TEXT NOT NULL,
        topic TEXT NOT NULL,
        descriptionHindi TEXT NOT NULL,
        descriptionSantali TEXT NOT NULL,
        isOfficial INTEGER NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS lessons (
        lessonId TEXT PRIMARY KEY NOT NULL,
        outcomeId TEXT NOT NULL,
        grade TEXT NOT NULL,
        domain TEXT NOT NULL,
        topic TEXT NOT NULL,
        titleHindi TEXT NOT NULL,
        titleSantali TEXT NOT NULL,
        instructionsJson TEXT NOT NULL,
        activitiesJson TEXT NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS flashcards (
        flashcardId TEXT PRIMARY KEY NOT NULL,
        outcomeId TEXT NOT NULL,
        titleHindi TEXT NOT NULL,
        titleSantali TEXT NOT NULL,
        iconEmoji TEXT NOT NULL,
        imagePath TEXT NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS worksheets (
        worksheetId TEXT PRIMARY KEY NOT NULL,
        outcomeId TEXT NOT NULL,
        titleHindi TEXT NOT NULL,
        titleSantali TEXT NOT NULL,
        questionsJson TEXT NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS students (
        studentId TEXT PRIMARY KEY NOT NULL,
        classId TEXT NOT NULL,
        name TEXT NOT NULL,
        rollNumber INTEGER NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS student_outcome_progress (
        id TEXT PRIMARY KEY NOT NULL,
        studentId TEXT NOT NULL,
        outcomeId TEXT NOT NULL,
        status TEXT NOT NULL,
        accuracyPercentage REAL NOT NULL,
        lastUpdated INTEGER NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS teaching_resources (
        resourceId TEXT PRIMARY KEY NOT NULL,
        classId TEXT NOT NULL,
        outcomeId TEXT NOT NULL,
        title TEXT NOT NULL,
        type TEXT NOT NULL,
        filePath TEXT NOT NULL,
        timestamp INTEGER NOT NULL
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS content_translations (
        contentId TEXT PRIMARY KEY NOT NULL,
        sourceLanguage TEXT NOT NULL,
        targetLanguage TEXT NOT NULL,
        translatedText TEXT NOT NULL,
        audioPath TEXT NOT NULL,
        status TEXT NOT NULL
    );
    """)

    # Populate Data from JSON
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    for c in data.get("classrooms", []):
        cursor.execute("INSERT INTO classrooms VALUES (?, ?, ?)", (c["classId"], c["grade"], c["name"]))

    for d in data.get("domains", []):
        cursor.execute("INSERT INTO domains VALUES (?, ?, ?)", (d["domainId"], d["name"], d["type"]))

    for comp in data.get("competencies", []):
        cursor.execute("INSERT INTO competencies VALUES (?, ?, ?, ?, ?)", (comp["competencyId"], comp["domainId"], comp["code"], comp["descriptionHindi"], comp["descriptionSantali"]))

    for lo in data.get("learningOutcomes", []):
        cursor.execute("INSERT INTO learning_outcomes VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", (lo["outcomeId"], lo["competencyId"], lo["nipunCode"], lo["grade"], lo["domain"], lo["topic"], lo["descriptionHindi"], lo["descriptionSantali"], 1 if lo["isOfficial"] else 0))

    for les in data.get("lessons", []):
        cursor.execute("INSERT INTO lessons VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", (les["lessonId"], les["outcomeId"], les["grade"], les["domain"], les["topic"], les["titleHindi"], les["titleSantali"], les["instructionsJson"], les["activitiesJson"]))

    for fc in data.get("flashcards", []):
        cursor.execute("INSERT INTO flashcards VALUES (?, ?, ?, ?, ?, ?)", (fc["flashcardId"], fc["outcomeId"], fc["titleHindi"], fc["titleSantali"], fc["iconEmoji"], fc["imagePath"]))

    for ws in data.get("worksheets", []):
        cursor.execute("INSERT INTO worksheets VALUES (?, ?, ?, ?, ?)", (ws["worksheetId"], ws["outcomeId"], ws["titleHindi"], ws["titleSantali"], ws["questionsJson"]))

    # Add Default Teacher & Students
    cursor.execute("INSERT INTO teachers VALUES (?, ?, ?, ?)", ("t_01", "Primary Teacher", '["class_1", "class_2", "class_3"]', 1700000000000))

    for i in range(1, 21):
        s_id = f"std_{i:02d}"
        s_name = f"Student {i:02d}"
        cursor.execute("INSERT INTO students VALUES (?, ?, ?, ?)", (s_id, "class_1", s_name, i))

    conn.commit()
    conn.close()
    print(f"Successfully generated reproducible database at {db_path}")

if __name__ == "__main__":
    build_db()
