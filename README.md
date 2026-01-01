# Budgiet
A budget tracking app with the frontend written in Kotlin for Android or Swift for iOS and
the backend written in Rust.

# Features

To be written up

# App Demo

To be shown

# Contribution Guidelines

Before contributing, there is a couple of things to keep in mind of:
* The following are named and reserved branches: 
    - `main` branch is supposed to be the stable app branch for Android and iOS version of Budgiet
    - `dev` branch contains the unstable/unpolished version of the app that is to be tested
* All stable feature PRs will first be merged into the `dev` branch, which will go through significant testing,
and then if everything is stabilized it will be merged into the `main` branch.

When making a contribution to this repository, follow this guideline:
* When implementing a feature or change to the repository, always create a pull request:
    - Add a meaningful and concise title + a detailed description regarding to what the feature is about
    - Make sure to write meaningful commits typically following the style of 
    (`"chore: ..."`, `"feat: ..."`, `"fix: ..."`, `"cleanup: ..."`, etc.)
    - Set the PR to merge with the `dev` branch
    - Note: If your PR pertains to multiple different features, please separate the changes that you're making
    into multiple different PRs.
* Always format your code accordingly to the language's style guidelines:
    - For Kotlin code, always format your code using Android Studio's `"Code -> Reformat Code"` option;
    - For Rust code, always format your code with `cargo fmt` and cleanup any warnings outputted by
    `cargo clippy`
    - For Swift code, (to be written)
* Always add test cases (if necessary) for the following feature/change to avoid future regression of the app
* Make sure that the current PR passes all existing CI tests
* If the PR is formatted properly and passes CI tests, you can request a review from either @Megadash452 or 
@asder8215

Making GitHub issues are a form of contribution as well! If there are any issues or desired features you would like 
to report for Budgiet, please make sure to include the following:
* A clear and concise title for the problem/feature
* A detailed description of the problem/feature
* If the problem can be visually seen, please provide a screenshot/small clip of the app to 
demonstrate the problem
* If an error message or output is visible, please paste or display the error in the issue
* Optionally, you can point out where the issue might occur in the app or how a feature should be
supported into the repository