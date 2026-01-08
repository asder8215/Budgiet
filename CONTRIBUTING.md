# Contribution Guidelines

Before contributing, there is a couple of things to keep in mind:
* The following are named and reserved branches: 
    - `main` branch is the stable app branch for Android and iOS versions of Budgiet.
    - `dev` branch contains the unstable/unpolished version of the app that is to be tested.
* All contributions must be made to the `dev` branch first,
  and can only be merged to the `main` branch after significant testing to ensure the code is stable.
  * All *new feature* contributions must be made to a new branch with the name of the feature.
    Once the feature is ready, a PR can be created to merge it to `dev`,
    which can then be merged to `main` after it is tested.

When making a contribution to this repository, follow these guidelines:
* When implementing a feature or change to the repository, always create a pull request:
    - Add a meaningful and concise title + a detailed description regarding what the feature is about.
    - Apply appropriate tags/labels on the PR or issue. (e.g. [`Task`](https://github.com/Soraoke/Budgiet/issues?q=type%3ATask), [`Feature`](https://github.com/Soraoke/Budgiet/issues?q=type%3AFeature), [`enhancement`](https://github.com/Soraoke/Budgiet/issues?q=label%3A%22enhancement%22), etc.).
    - Write meaningful commit messages.
    - Set the PR to merge with the `dev` branch.
    - When merging a PR, use the following format for the merge commit message: `Merge #<PR_NUMBER>: <PR_TITLE>`.
    > **Note**: If your PR pertains to multiple different features, please separate the changes that you're making
    into multiple different PRs.
* Always format your code accordingly to the language's style guidelines:
    - For Kotlin code, format your code using Android Studio's `"Code -> Reformat Code"` option;
    - For Rust code, always format your code with `cargo fmt` and cleanup any warnings outputted by `cargo clippy`.
    - For Swift code, format your code using `swift-format`.
* Always add test cases (if necessary) for the feature/change to avoid future regression of the app.
* Make sure that the current PR passes all existing CI tests.
* If the PR is formatted properly and passes CI tests, you can request a review from either [@Megadash452](https://github.com/Megadash452) or 
[@asder8215](https://github.com/asder8215).

Making GitHub issues is a form of contribution as well!
If there are any issues or desired features you would like to report for Budgiet,
please make sure to include the following:
* A clear and concise title for the problem/feature.
* A detailed description of the problem/feature.
* If the problem can be visually seen, please provide a screenshot/small clip of the app to demonstrate the problem.
* If an error message or output is visible, please paste or display the error in the issue.
* If you are able to, paste logs related to the problem.
* Optionally, you can point out a specific area of the frontend or backend that a problem/feature could be located.
