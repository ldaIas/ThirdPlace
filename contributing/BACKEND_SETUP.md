# Steps to setup a development environment for the backend

## Server

- Ensure you have Java 21 installed. Available at <https://www.oracle.com/java/technologies/downloads/#java21> (open command prompt/terminal and type java -version to check)
- Clone this repo to your preferred location. Open terminal and navigate to `.../thirdplace/tp-api/`
- Copy the path location of the Java 21 installation, and add it to the "java.lds.home" setting in .vscode/settings.json. You can create this file by copying the settings.json.template file.
- If using VScode, install the Java Language extension pack. Available at <https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack>
- Gradle may not be immediately visible as an extension in vscode. Try creating a new gradle project with Ctrl+Shift+P -> Gradle: Create Gradle Java Project (Advanced) and follow the steps. See <https://github.com/microsoft/vscode-gradle/issues/1462> for more
- Ensure you can build and run by using `sh gradlew build` and `sh gradlew run`

## PostgreSQL

### Download

- Download postgres 17 from the appropriate OS here <https://www.postgresql.org/download/>
  - If using Mac you can run `brew install postgresql`
- Run the installer or let it install if using brew
- If using Windows, the database should have been setup in c:\Program Files\postgreSQL. If not, use the command below to initialize
- If using Mac/Unix, the database should have been setup in /usr/local/var/postgreSQL. If not, use the command below to initialize
- Run `initdb <path to db>` to initialize a postgres database in a specific directory. \<path to db\> is the path on your system where postgres will store database contents

### Create Postgres user

- Ensure the existance of a postgres user. Use `psql postgres` to login to the database.
- While in `psql`, run `CREATE ROLE postgres WITH SUPERUSER LOGIN PASSWORD '3310';`
- Verify the postgres user was created by exiting psql and running `psql -U postgres`

CODACY

- Codacy (<https://www.codacy.com/>) is a code quality tool. It is integrated in the GitHub repo to run analyses on PRs.
- Codacy has extensions available on various IDEs, including vscode: <https://marketplace.visualstudio.com/items?itemName=codacy-app.codacy>.
    It is recommended to install the extension to gain insight into code quality as you write

Add to this file if you find info is missing/incorrect or if additional info would be helpful.
