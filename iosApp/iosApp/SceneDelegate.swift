import UIKit
import MarkdownEditor

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }
        window = UIWindow(windowScene: windowScene)
        let config = MarkdownEditorConfig()
        config.placeholder = "Tap here to start writing..."
        config.onContentChanged = { _ in }

        window?.rootViewController = MarkdownEditorViewControllerKt.MarkdownEditorViewController(
            controller: MarkdownEditorController(),
            config: config
        )
        window?.makeKeyAndVisible()
    }
}
