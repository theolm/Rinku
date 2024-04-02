#!/bin/bash

# Prompt user for project name
read -p "Enter new module name: " module_name

# Prompt user for application id
read -p "What kind of module you would like to create: [1] Compose [2] KMM: " option

case $option in
1)
  plugin_name=compose-module-setup
  ;;
2)
  plugin_name=kmm-module-setup
  ;;
*)
  exit 1
  ;;
esac

# checking module_name
if [ -z "${module_name}" ]; then
    echo "Empty module name..."
    exit 1
fi

# checking option
if [ -z "${plugin_name}" ]; then
    echo "Invalid option..."
    exit 1
fi

# creating directories
mkdir -p "${module_name}/src/commonMain/kotlin"

echo -e "fun welcomeToYourNewModule() {\n \tprintln(\"hello world\")\n}" > "${module_name}/src/commonMain/kotlin/HelloWorld.kt"

echo -e "plugins { \n\tid(\"${plugin_name}\") \n}" > "${module_name}/build.gradle.kts"
echo -e "\nandroid {\n\tnamespace = \"${module_name}\" \n} " >> "${module_name}/build.gradle.kts"
echo -e "\ninclude(\":${module_name}\")" >> "settings.gradle.kts"

echo "You can sync your project now!"