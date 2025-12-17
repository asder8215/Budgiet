# Budgiet

A budget tracking app written in Flutter and Rust.

# Developer Setup

1. Install dependencies:
    <details>
      <summary>Ubuntu/Debian</summary>

      ```sh
      sudo apt-get update -y && sudo apt-get upgrade -y
      sudo apt-get install curl git unzip xz-utils zip libglu1-mesa -y
      # If running Android Studio
      sudo apt-get install libc6 libncurses5 libstdc++6 lib32z1 libbz2-1.0 -y
      # If building for Linux
      sudo apt-get install clang cmake ninja-build pkg-config libgtk-3-dev libstdc++-12-dev -y
      ```
    </details>
    <details>
      <summary>Fedora</summary>

      ```sh
      sudo dnf install curl git unzip xz zip mesa-libGLU -y
      # If running Android Studio
      sudo dnf install zlib ncurses-libs bzip2-libs -y
      # If building for Linux
      sudo dnf install clang cmake ninja-build pkg-config gtk3-devel -y
      ```
    </details>
    <details>
      <summary>NixOS</summary>

      On NixOs you just need to import the config in [`flutter.nix`](./flutter.nix).
      You can copy the file to `/etc/nixos` and import it from `configuration.nix` by adding it to the **imports** list:
      ```nix
        imports = [
          ./flutter.nix
        ];
      ```
    </details>

2. Install Flutter:
    > If you are using **VS Code/Codium**, you can skip this step and simply install the [Flutter extension](https://marketplace.visualstudio.com/items?itemName=Dart-Code.flutter).
    > From there, run `>flutter doctor` in the command pallete (press F1) and click on `Install Flutter` in the popup.
    > You can follow a more detailed explanation in the [flutter docs](https://docs.flutter.dev/get-started/quick#install).

    ```sh
    curl "https://storage.googleapis.com/flutter_infra_release/releases/stable/linux/flutter_linux_3.38.1-stable.tar.xz" -o flutter.tar.xz
    mkdir -p ~/.local/lib/
    tar -xf flutter.tar.xz --directory ~/.local/lib/
    rm flutter.tar.xz
    # Add to PATH
    # NOTE: If your shell does not source .profile, this will not work and you will have to add the path manually.
    echo 'export PATH="$HOME/.local/lib/flutter/bin:$PATH"' >> ~/.profile
    source ~/.profile
    ```

3. Install **Android Studio**:
    ```sh
    # Android Studio
    prev_dir=$(pwd); cd $(mktemp -d)
    install_dir="/opt"
    read -p "Enter download link to Android studio from https://developer.android.com/studio:" url
    wget --output-document android-studio.tar.gz "$url"
    tar -xf android-studio.tar.gz
    sudo mv ./android-studio "$install_dir"
    sudo sh -c 'echo "[Desktop Entry]
    Name=Android Studio
    Comment=Integerated Development Environment for Android
    Exec="/opt/android-studio/bin/studio"
    Icon=/opt/android-studio/bin/studio.svg
    Terminal=false
    Type=Application
    StartupWMClass=jetbrains-studio
    Categories=Development;IDE" > /usr/local/share/applications/android-studio.desktop'
    cd "$prev_dir"
    ```

    * You might also need to manually enable virtualization.
      Try running an emulator, and if it doesn't work, run this:
      <details>
        <summary>Ubuntu/Debian</summary>

        TODO: Enable virtualization in ubuntu
      </details>
      <details>
        <summary>Fedora</summary>
        
        ```sh
        sudo dnf group install --with-optional virtualization -y
        sudo systemctl enable libvirtd
        if [[ ! $(lsmod | grep kvm) ]]; then
          printf "\033[0;WARNING: KVM for Android Studio was NOT setup correctly. Check if your hardware supports KVM.\033[0m\n" >&2
        fi
        ```
      </details>

4. Install **Android** runtime:
    ```sh
    # Install Android commandline tools
    curl "https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip" -o android-cmd-tools.zip
    mkdir -p ~/Android/Sdk/cmdline-tools/
    unzip android-cmd-tools.zip -d ~/Android/Sdk/cmdline-tools/
    mv ~/Android/Sdk/cmdline-tools/cmdline-tools ~/Android/Sdk/cmdline-tools/latest
    rm android-cmd-tools.zip
    # Install Android runtimes
    ~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager --install "platforms;android-36" "build-tools;36.1.0" "platform-tools" "emulator" "system-images;android-35;google_apis_playstore;x86_64"
    ```
    > You can set up a *device or emulator* to run the code with **Android Studio** by following [these steps](https://docs.flutter.dev/platform-integration/android/setup#set-up-devices) in the flutter doc.
    > The *VS Code* extension might also give you the option to create an emulator in `>Flutter: Select Device`, but this doesn't always show up.
    > Otherwise, you will have to set up an emulator with [these steps](https://github.com/maiz-an/AVD-Setup-without-Andriod-Studio#3-install-system-images-and-create-an-avd).

5. Install **IOS** runtime (**Mac Only**):
    TODO:

6. Finally, agree to licenses and do validation:
    ```sh
    flutter doctor --android-licenses
    flutter emulators && flutter devices
    # TODO: IOS
    ```
