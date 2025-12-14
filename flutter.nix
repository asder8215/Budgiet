# Taken from https://dev.to/shanu-kumawat/setting-up-flutter-development-environment-on-nix-6pk
# NOTE: This file is imported from `configuration.nix`
{ pkgs, config, lib, ... }:
let
  cfg = config.programs.flutter;
  buildToolsVersion = "33.0.1";
  androidComposition = pkgs.androidenv.composeAndroidPackages {
    toolsVersion = "26.1.1";
    platformToolsVersion = "36.0.0";
    buildToolsVersions = [
      "36.0.0"
    ];
    platformVersions = [
      "36"
    ];
    emulatorVersion = "36.3.9";
    abiVersions = [ "x86_64" ];
    includeEmulator = true;
    includeSystemImages = true;
    systemImageTypes = [ "google_apis_playstore" ];
    includeSources = false;
    extraLicenses = [
      "android-googletv-license"
      "android-sdk-arm-dbt-license"
      "android-sdk-license"
      "android-sdk-preview-license"
      "google-gdk-license"
      "intel-android-extra-license"
      "intel-android-sysimage-license"
      "mips-android-sysimage-license"
    ];
  };
  androidSdk = androidComposition.androidsdk;
in {
  options.programs.flutter = {
    enable = lib.mkEnableOption "Flutter development environment";
    addToKvmGroup = lib.mkEnableOption "Add user to KVM group for hardware acceleration";
    enableAdb = lib.mkEnableOption "Enable ADB and add user to adbusers group";
    user = lib.mkOption {
      type = lib.types.str;
      description = "Username for Flutter development";
    };
  };

  config = lib.mkIf cfg.enable {
    environment.systemPackages = [
      pkgs.flutter pkgs.androidSdk pkgs.jdk17
      # pkgs.android-studio
      # pkgs.firebase-tools
    ];

    environment.variables = {
      ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";
      ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
      JAVA_HOME = "${pkgs.jdk17}";
      GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidSdk}/libexec/android-sdk/build-tools/34.0.0/aapt2";
    };

    nixpkgs.config = {
      android_sdk.accept_license = true;
      allowUnfree = true;
    };

    environment.shellInit = ''
      export PATH=$PATH:${androidSdk}/libexec/android-sdk/platform-tools
      export PATH=$PATH:${androidSdk}/libexec/android-sdk/cmdline-tools/latest/bin
      export PATH=$PATH:${androidSdk}/libexec/android-sdk/emulator
      export PATH="$PATH":"$HOME/.pub-cache/bin"
    '';

    programs.adb.enable = cfg.enableAdb;

    users.users.${cfg.user}.extraGroups =
      (lib.optional cfg.addToKvmGroup "kvm") ++ (lib.optional cfg.enableAdb "adbusers");
  };
}
