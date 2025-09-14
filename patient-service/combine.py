import os
import sys

def dump_project_files(root_folder, output_file, skip_extensions=None, skip_files=None, skip_folders=None, module_name=None):
    """
    Recursively reads all files in a project, skipping specified items.
    It can also group files based on a common module prefix (e.g., 'Patient').
    """

    if skip_extensions is None: skip_extensions = []
    if skip_files is None: skip_files = []
    if skip_folders is None: skip_folders = []

    all_files_data = []
    module_files_data = []
    file_count = 0

    print("🔍 Scanning project files...")

    # First pass: Walk the directory and collect all relevant file data
    for foldername, subfolders, filenames in os.walk(root_folder):
        # Skip unwanted folders completely
        # A bit of magic to modify subfolders in-place to prevent os.walk from traversing them
        subfolders[:] = [d for d in subfolders if not any(skip in os.path.join(foldername, d).lower().replace("\\", "/") for skip in skip_folders)]

        for filename in filenames:
            file_path = os.path.join(foldername, filename)

            # Check for skips
            if os.path.splitext(filename)[1].lower() in skip_extensions: continue
            if filename.lower() in skip_files: continue

            relative_path = os.path.relpath(file_path, root_folder).replace("\\", "/")

            try:
                with open(file_path, "r", encoding="utf-8", errors="ignore") as infile:
                    content = infile.read().strip()
                    if content:
                        file_data = (relative_path, content)
                        all_files_data.append(file_data)
                        file_count += 1

                        # New Feature: Check if the file belongs to the specified module
                        if module_name and os.path.basename(filename).lower().startswith(module_name.lower()):
                            module_files_data.append(file_data)

            except Exception as e:
                print(f"⚠️  Could not read file: {relative_path} ({e})")

    print(f"✍️  Writing {file_count} files to {output_file}...")

    # Second pass: Write collected data to the output file
    with open(output_file, "w", encoding="utf-8") as outfile:
        outfile.write("=== SPRING BOOT PROJECT DUMP ===\n")
        outfile.write(f"Root: {root_folder}\n")
        if module_name:
            outfile.write(f"Module Grouping: {module_name.capitalize()}\n")
        outfile.write("="*50 + "\n")

        # New Feature: If a module was specified and files were found, write the grouped section first
        if module_name and module_files_data:
            outfile.write(f"\n\n=== MODULE GROUP: {module_name.capitalize()} ===\n")
            outfile.write(f"Found {len(module_files_data)} files related to '{module_name.capitalize()}'.\n")
            outfile.write("="*50 + "\n")
            for relative_path, content in sorted(module_files_data):
                outfile.write(f"\n\n--- FILE: {relative_path} ---\n")
                outfile.write(content)
                outfile.write(f"\n--- END OF {relative_path} ---\n")
            outfile.write("\n" + "="*50 + "\n")

        # Write all project files (original logic)
        outfile.write(f"\n\n=== ALL PROJECT FILES ===\n")
        outfile.write("="*50 + "\n")
        for relative_path, content in sorted(all_files_data):
            outfile.write(f"\n\n--- FILE: {relative_path} ---\n")
            outfile.write(content)
            outfile.write(f"\n--- END OF {relative_path} ---\n")

        outfile.write(f"\n\n=== SUMMARY ===\n")
        outfile.write(f"Total files processed: {file_count}\n")


if __name__ == "__main__":
    # Check if a root folder is provided as a command-line argument
    if len(sys.argv) > 1:
        root_folder = sys.argv[1]
        if not os.path.isdir(root_folder):
            print(f"❌ Error: The provided path '{root_folder}' is not a valid directory.")
            sys.exit(1)
    else:
        # Default root folder if no argument is given
        root_folder = "." # Scans the current directory

    # Output file for AI reference
    output_file = "springboot_project_dump.txt"

    # Extensions to skip
    skip_extensions = [
        ".class", ".jar", ".war", ".ear", ".zip", ".tar", ".gz", ".rar", ".7z",
        ".jpg", ".jpeg", ".png", ".gif", ".ico", ".bmp", ".svg", ".webp",
        ".mp3", ".mp4", ".avi", ".mov", ".pdf", ".exe", ".dll", ".so", ".log",
        ".tmp", ".temp", ".cache", ".pid", ".ttf", ".woff", ".woff2", ".eot",
        ".keystore", ".jks"
    ]

    # Specific files to skip
    skip_files = [
        ".ds_store", "thumbs.db", "mvnw", "mvnw.cmd", "gradlew", "gradlew.bat",
        ".project", ".classpath", "package-lock.json", "yarn.lock"
    ]

    # Entire folders to skip
    skip_folders = [
        "target", "build", "out", "bin", ".idea", ".vscode", ".git", ".svn",
        "node_modules", ".gradle", ".m2", "logs", ".mvn"
    ]

    # New Feature: Get module name from user input
    try:
        module_name_input = input("🚀 Enter a module name to group (e.g., Patient), or press Enter to skip: ").strip()
    except KeyboardInterrupt:
        print("\nOperation cancelled by user.")
        sys.exit(0)


    print(f"\n🚀 Starting Spring Boot project dump...")
    print(f"📁 Scanning: {os.path.abspath(root_folder)}")
    if module_name_input:
        print(f"🧬 Grouping module: {module_name_input.capitalize()}")

    dump_project_files(
        root_folder=root_folder,
        output_file=output_file,
        skip_extensions=skip_extensions,
        skip_files=skip_files,
        skip_folders=skip_folders,
        module_name=module_name_input
    )

    print(f"\n✅ Spring Boot project dumped into: {output_file}")
    print(f"📊 You can now use this file for AI analysis or documentation.")
