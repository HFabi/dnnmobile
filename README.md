# Deep neural networks on mobile
> __This repo is work in progress and currently migrated here from my several private repos__

This repository is meant to be a playground for mobile deep learning. It compares and analyzes state-of-the-art mobile deep learning frameworks with focus on Android, iOS and mobile browsers.

The project is structured as a mono-repo and consists of the following sections:
- `dnnmobile-app-android`: contains the android app
- `dnnmobile-app-ios`: contains the ios app
- `dnnmobile-app-web`: contains the web app
- `common`: contains the preprocessing of datasets and training of neural network models in form of docker containers and jupyter notebooks.


## Exploring mobile deep learing
Mainly, there are there populare mobile deep learning libraries Tensorflow Lite, PyTorch Mobile as well as Core ML, all having differences in their capabilities.

| Library         | Company  | Platform               | on-device inference | on-device training |
| --------------- | -------- | ---------------------- | ------------------- | ------------------ |
| Tensorflow Lite | Google   | Android, iOS, embedded | &check;             | &check; (transfer)           |
| PyTorch Mobile  | Facebook | Android, iOS           | &check;             | &cross;            |
| Core ML         | Apple    | iOS                    | &check;             | &check; (transfer)            |

## Setup
tba.


## Roadmap
- [ ] add pytorch preprocessing
- [ ] add pytorch to android
- [ ] add iOS project
- [ ] add web project
- [ ] add datasets
- [ ] extend documentation
- [ ] add popular models (e.g. MobileNet)
- [ ] add benchmarking
- [ ] tba.
