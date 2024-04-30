import SwiftUI
import ComposeApp

@UIApplicationMain
class AppDelegate: NSObject, UIApplicationDelegate {
    var window: UIWindow?
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        self.window = UIWindow(frame: UIScreen.main.bounds)
        let mainViewController = UIHostingController(rootView: ContentView())
        self.window!.rootViewController = mainViewController
        self.window!.makeKeyAndVisible()
        
        return true
    }
    
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        RinkuIosKt.onDeepLinkReceived(url: url.absoluteString, deepLinkFilter: TestFilter.shared)
        return true
    }
    
    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        RinkuIosKt.onDeepLinkReceived(userActivity: userActivity, deepLinkFilter: TestFilter.shared)
        return true
    }
}
