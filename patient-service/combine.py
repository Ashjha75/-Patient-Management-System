import os

def dump_project_files(root_folder, output_file, skip_extensions=None, skip_files=None, skip_folders=None):
    """
    Recursively reads all files in a Spring Boot project (IntelliJ based),
    skipping binaries, build artifacts, and unnecessary files.
    Optimized for Java/Spring Boot development.
    """

    if skip_extensions is None:
        skip_extensions = []
    if skip_files is None:
        skip_files = []
    if skip_folders is None:
        skip_folders = []

    file_count = 0
    with open(output_file, "w", encoding="utf-8") as outfile:
        outfile.write("=== SPRING BOOT PROJECT DUMP ===\n")
        outfile.write(f"Root: {root_folder}\n")
        outfile.write("="*50 + "\n\n")

        for foldername, subfolders, filenames in os.walk(root_folder):

            # Skip unwanted folders completely
            if any(skip in foldername.lower() for skip in skip_folders):
                continue

            for filename in filenames:
                file_ext = os.path.splitext(filename)[1].lower()

                # Skip unwanted extensions or filenames
                if file_ext in skip_extensions or filename.lower() in skip_files:
                    continue

                file_path = os.path.join(foldername, filename)
                relative_path = os.path.relpath(file_path, root_folder)

                try:
                    with open(file_path, "r", encoding="utf-8", errors="ignore") as infile:
                        content = infile.read().strip()
                        if content:  # Only include non-empty files
                            outfile.write(f"\n\n=== FILE: {relative_path} ===\n")
                            outfile.write(f"Full path: {file_path}\n")
                            outfile.write("-" * 50 + "\n")
                            outfile.write(content)
                            outfile.write("\n" + "-" * 50)
                            file_count += 1
                except Exception as e:
                    outfile.write(f"\n\n=== FILE: {relative_path} (Could not read: {e}) ===\n\n")

        outfile.write(f"\n\n=== SUMMARY ===\n")
        outfile.write(f"Total files processed: {file_count}\n")


if __name__ == "__main__":
    # 👇 Change this to your Spring Boot project root folder
    root_folder = r"C:/Users/Ashish jha/Desktop/PERSONAL/Patient-Management-System/patient-service/src/main/java/com/patientmanagement/patientservice/security"

    # Output file for AI reference
    output_file = "springboot_project_dump.txt"

    # Extensions to skip (binaries, compiled files, media, etc.)
    skip_extensions = [
        # Compiled/Binary files
        ".class", ".jar", ".war", ".ear", ".zip", ".tar", ".gz", ".rar", ".7z",
        # Media files
        ".jpg", ".jpeg", ".png", ".gif", ".ico", ".bmp", ".svg", ".webp",
        ".mp3", ".mp4", ".avi", ".mov", ".wmv", ".pdf",
        # IDE/OS files
        ".exe", ".dll", ".so", ".dylib", ".db", ".sqlite", ".h2.db",
        # Logs and temp files
        ".log", ".tmp", ".temp", ".cache", ".pid",
        # Fonts
        ".ttf", ".woff", ".woff2", ".eot",
        # Other
        ".keystore", ".p12", ".jks"
    ]

    # Specific files to skip
    skip_files = [
        # OS files
        ".ds_store", "thumbs.db", "desktop.ini",
        # Maven/Gradle lock files
        "mvnw", "mvnw.cmd", "gradlew", "gradlew.bat",
        # IDE files
        ".project", ".classpath", "*.iws", "*.ipr",
        # Build files
        "package-lock.json", "yarn.lock"
    ]

    # Entire folders to skip (Spring Boot specific)
    skip_folders = [
        # Build directories
        "target", "build", "out", "bin", "classes",
        # IDE directories
        ".idea", ".vscode", ".eclipse", ".metadata", ".settings",
        # Version control
        ".git", ".svn", ".hg",
        # Dependencies
        "node_modules", ".gradle", ".m2",
        # Logs
        "logs", "log",
        # Spring Boot specific
        ".mvn", "mvnw.cmd",
        # Other
        "temp", "tmp", ".cache", "coverage", "dist"
    ]

    print(f"🚀 Starting Spring Boot project dump...")
    print(f"📁 Scanning: {root_folder}")

    dump_project_files(root_folder, output_file, skip_extensions, skip_files, skip_folders)

    print(f"✅ Spring Boot project dumped into: {output_file}")
    print(f"📊 You can now use this file for AI analysis or documentation.")
