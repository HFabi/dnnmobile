//
//  dnnmobileApp.swift
//  dnnmobile
//
//  Created by Fabian on 09.03.23.
//

import SwiftUI

@main
struct dnnmobileApp: App {
    
    let benchmarkViewModel = ContentViewModel()
    
    var body: some Scene {
        WindowGroup {
            ContentView(viewModel: benchmarkViewModel)
        }
    }
}
