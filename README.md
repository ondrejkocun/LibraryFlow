# LibraryFlow

LibraryFlow is a Java desktop library management system developed as a university semester project.

The project was created as a practical way to learn Java programming, object-oriented design, polymorphism, file handling, CSV persistence, authentication, and Swing GUI development.

## Features

- User login with role-based access
- Administrator and regular user modes
- Multiple user types with different borrowing policies:
  - Administrator
  - Standard user
  - Student
  - VIP user
  - Retired user
- Multiple media types:
  - Books
  - Films
  - Audiobooks
  - E-books
  - Magazines
  - Reference literature
- Borrowing, returning, and reserving media
- Borrowing limits based on user type
- Due dates and overdue fines
- Loan ownership checks
- Persistent media and user data in CSV files
- Persistent loan history
- Search and filtering
- Statistics and administrator dashboard
- Swing desktop interface
- Basic executable regression tests

## Project Structure

```text
library/
  LibrarySystem.java       Core domain and library operations
  media/                   Media types and borrowing rules
  users/                   User interface, policies, and user types
  persistence/             CSV storage, logging, and loan history
  services/                Search and statistics services
  ui/                      Swing windows and dialogs
  exceptions/              Custom exceptions
  tests/                   Executable regression tests

media.csv                 Media data
users.csv                 User accounts and password hashes
loan-history.csv          Loan history created during application use
```

## Requirements

- Java Development Kit 17 or newer
- Windows, macOS, or Linux

## Running the Application

From the project root, compile all Java sources:

```powershell
javac -d out (Get-ChildItem library -Recurse -Filter *.java).FullName
```

Run the Swing application:

```powershell
java -cp out library.ui.MainGUI
```

On macOS or Linux, use:

```bash
javac -d out $(find library -name "*.java")
java -cp out library.ui.MainGUI
```

## Test Accounts

The sample CSV contains 51 users. The administrator account is:

```text
User ID: ADMIN
Password: admin
```

Other sample accounts include:

```text
U-001 / alex
U-002 / victoria
U-003 / robert
```

Passwords are stored in `users.csv` as SHA-256 hashes with the `sha256$` prefix. The sample passwords above are provided only for local educational testing.

## Running Tests

Compile the project and run the executable regression test with assertions enabled:

```powershell
javac -d out (Get-ChildItem library -Recurse -Filter *.java).FullName
java -ea -cp out library.tests.LibrarySystemTest
```

Expected output:

```text
All library tests passed.
```

## Learning Goals

This project was built while learning Java and focuses on applying core programming concepts in practice:

- Object-oriented programming
- Interfaces and abstract classes
- Polymorphism across media and user types
- Encapsulation and role-based access
- Collections and domain modeling
- File input/output and CSV persistence
- Exception handling and validation
- Swing GUI development
- Basic testing and project organization

## Educational Note

LibraryFlow is a learning project created as part of a university semester assignment. It is intentionally simple and is not intended for production use. The authentication and CSV storage are suitable for local experimentation, but a production system would require stronger password storage, a database, audit logging, and more comprehensive automated tests.

## License

This project is provided for educational and portfolio purposes.
