# Account Logging Project

This project was created to securely encrypt account details, including passwords and usernames.

The Java application follows the lock-and-key model, where there is a key in the form of a .key file and a database in the form of a .db file. Specific rows within the database are encrypted. Note that the .db file itself is not encrypted.

## Requirements

- Java 14 and up
- sql lite jdbc (for development)

## Features
- Utilizes AES 256 GCM encryption standard.
- Allows searching based on platforms.
- Supports exporting of accounts via a text file.
- export to JSON, options include encrypted or unencrypted
- importing via JSON with the same format as the exported
- Provides basic CRUD operations on accounts.
- Tracks history updates of accounts and the capability to delete specific and all history.
