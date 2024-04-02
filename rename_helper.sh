#!/bin/bash

# Prompt user for project name
read -p "Enter project name: " project_name

# Prompt user for application id
read -p "Enter application id: " app_id

# Check if the correct number of arguments is provided
if [ -z "$project_name" ] || [ -z "$app_id" ]; then
    echo "Error: Project name and application id are required."
    exit 1
fi

app_id_folder_structure=$(echo "$app_id" | tr '.' '/')

# Print the values of the variables
echo "Project Name: $project_name"
echo "Application ID: $app_id"
echo "Application ID Folder Structure: $app_id_folder_structure"

# Escape special characters in the project name for sed
escaped_project_name=$(sed 's/[^^]/[&]/g; s/\^/\\^/g' <<<"$project_name")

# Update the rootProject.name field in settings.gradle.kts
settings_file="settings.gradle.kts"
if [ -f "$settings_file" ]; then
    sed -i '' -e "s/rootProject.name = .*/rootProject.name = \"$project_name\"/g" "$settings_file"
    echo "rootProject.name updated in $settings_file"
else
    echo "Error: $settings_file not found."
fi

# Update the BUNDLE_ID and APP_NAME fields in iosApp/Configuration/Config.xcconfig
xcconfig_file="iosApp/Configuration/Config.xcconfig"
if [ -f "$xcconfig_file" ]; then
    sed -i '' -e "s/BUNDLE_ID=.*/BUNDLE_ID=$app_id/g" "$xcconfig_file"
    sed -i '' -e "s/APP_NAME=.*/APP_NAME=$project_name/g" "$xcconfig_file"
    echo "BUNDLE_ID and APP_NAME updated in $xcconfig_file"
else
    echo "Error: $xcconfig_file not found."
fi

# Update the applicationId field in build-logic/src/main/kotlin/config/Config.kt
config_file="build-logic/src/main/kotlin/config/Config.kt"
if [ -f "$config_file" ]; then
    sed -i '' -e "s/const val applicationId = .*/const val applicationId = \"$app_id\"/g" "$config_file"
    echo "applicationId updated in $config_file"
else
    echo "Error: $config_file not found."
fi

# Create directory structure for composeApp/src/androidMain/kotlin
android_main_folder="composeApp/src/androidMain/kotlin"
new_complete_path="$android_main_folder/$app_id_folder_structure"
mkdir -p $new_complete_path

echo "Directory structure created for $app_id_folder_structure in $android_main_folder"

# Move contents from composeApp/src/androidMain/kotlin/com/theolm/temp to the new directory
mv "$android_main_folder/com/theolm/temp"/* "$new_complete_path"
rmdir "$android_main_folder/com/theolm/temp"

echo "Directory structure created for $app_id_folder_structure in $android_main_folder"
echo "Contents moved to $new_complete_path"

# Replace the package name in Kotlin files
find "$new_complete_path" -type f -name "*.kt" -exec sed -i '' -e "s/package com.theolm.temp/package $app_id/g" {} +


# Replace the title in composeApp/src/desktopMain/kotlin/main.kt
desktop_main_file="composeApp/src/desktopMain/kotlin/main.kt"
if [ -f "$desktop_main_file" ]; then
    sed -i '' -e "s/title = \"Temp\"/title = \"$project_name\"/g" "$desktop_main_file"
    echo "Title updated in $desktop_main_file"
else
    echo "Error: $desktop_main_file not found."
fi

# Rename the parent folder with the project name
parent_folder=$(basename "$(pwd)")
new_parent_folder="${project_name}"

mv "$(pwd)" "../$new_parent_folder"
echo "Parent folder renamed to $new_parent_folder"
