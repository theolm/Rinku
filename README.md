# Template for Compose Multiplatform Apps

This template provides:
- Support for Compose multiplatform as well as KMP only;
- Support for Android, iOS, and Desktop;
- Pre-compiled scripts for Compose modules and KMP modules;
- API client Module using Ktor;
- Dependency injection using Koin;
- Navigation using Voyager;
- Helper scripts in bash;

### Usage

Create a new repo using this template as a starting point. After that, you will need to change some things like the name of the project, bundleId, applicationId, etc.

If you choose to do it manually, you will need to follow these steps:
- In the `settings.gradle.kts`, rename the `rootProject.name` and the parent folder of your project. Please keep in mind that the `rootProject.name` and the parent folder need to have the same value;
- Open the file `iosApp/Configuration/Config.xcconfig` and change the BUNDLE_ID and APP_NAME;
- In the file `build-logic/src/main/kotlin/config/Config.kt`, change the applicationId. Use a valid pattern;
- In the folder `composeApp/src/androidMain/kotlin`, change the package structure of the application. It needs to follow the applicationId pattern. You can use the AS rename feature for that;
- Change the package in every file inside `composeApp/src/androidMain/kotlin` to follow the structure you just created. AS can do that too;
- In the `composeApp/src/desktopMain/kotlin/main.kt`, change the title of the Application. This title will be displayed in your application window.
- Sync/build the project;

You can also do this by using the script `rename_helper.sh`. You will have to inform the name of the project and the new applicationId (please provide a valid pattern). Keep in mind this script uses the template values as tokens, so it will work only once. If you need to run it more than once, you will have to rollback the changes or clone the repo again.
