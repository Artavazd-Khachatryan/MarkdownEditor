// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "MarkdownEditor",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "MarkdownEditor",
            targets: ["MarkdownEditor"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "MarkdownEditor",
            url: "https://github.com/Artavazd-Khachatryan/MarkdownEditor/releases/download/1.0.0-alpha02/MarkdownEditor.xcframework.zip",
            checksum: "3a51f2894e3b305b7999efd865b1fbbf580194cedc35c48c156da7dbe192b974"
        )
    ]
)
