//
//  ContentView.swift
//  dnnmobile
//
//  Created by Fabian on 09.03.23.
//

import SwiftUI

struct ContentView: View {
    
    var viewModel: ContentViewModel
    
    var body: some View {
        VStack {
            Button(action: {viewModel.runCoreMlMnist()}) {
                Text("sample")
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        let vm = ContentViewModel()
        ContentView(viewModel: vm)
    }
}
