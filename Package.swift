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
            url: "https://github.com/Artavazd-Khachatryan/MarkdownEditor/releases/download/1.0.0/MarkdownEditor.xcframework.zip",
            checksum: "REPLACE_WITH_CHECKSUM_AFTER_RELEASE"
        )
    ]
)
