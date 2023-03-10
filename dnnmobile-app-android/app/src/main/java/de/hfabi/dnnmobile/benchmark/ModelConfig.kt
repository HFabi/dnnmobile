package de.hfabi.dnnmobile.benchmark

/**
 * Model Config
 *
 * Description of data sets and corresponding models.
 */
object ModelConfig {

    private const val dataDir = "datasets"
    private const val tfModelDir = "tensorflow"
    private const val ptModelDir = "pytorch"

    open class Dataset(
        var name: String,
        val trainInput: String,
        val trainTarget: String,
        val testInput: String,
        val testTarget: String,
        val featuresInput: Int,
        val featuresTarget: Int,
        val trainSampleCount: Int,
        val testSampleCount: Int
    )

    object FashionMnistSmall : Dataset(
        name = "Fashion Mnist Small",
        trainInput = "$dataDir/fashion_mnist_600_train_input.csv",
        trainTarget = "$dataDir/fashion_mnist_600_train_target_categorical.csv",
        testInput = "$dataDir/fashion_mnist_100_test_input.csv",
        testTarget = "$dataDir/fashion_mnist_100_test_target_categorical.csv",
        featuresInput = 784,
        featuresTarget = 10,
        trainSampleCount = 600,
        testSampleCount = 100
    )

    object BreastCancerWisconsin : Dataset(
        name = "Breast Cancer Wisconsin",
        trainInput = "$dataDir/breastcancer_train_input.csv",
        trainTarget = "$dataDir/breastcancer_train_target.csv",
        testInput = "$dataDir/breastcancer_test_input.csv",
        testTarget = "$dataDir/breastcancer_test_target.csv",
        featuresInput = 30,
        featuresTarget = 1,
        trainSampleCount = 398,
        testSampleCount = 171
    )

    object Mnist : Dataset(
        name = "Mnist",
        trainInput = "$dataDir/mnist_train_input.csv",
        trainTarget = "$dataDir/mnist_train_target_categorical.csv",
        testInput = "$dataDir/mnist_test_input.csv",
        testTarget = "$dataDir/mnist_test_target_categorical.csv",
        featuresInput = 784,
        featuresTarget = 10,
        trainSampleCount = 60000,
        testSampleCount = 10000
    )

    object MnistSmall : Dataset(
        name = "Mnist",
        trainInput = "$dataDir/mnist_600_train_input.csv",
        trainTarget = "$dataDir/mnist_600_train_target_categorical.csv",
        testInput = "$dataDir/mnist_100_test_input.csv",
        testTarget = "$dataDir/mnist_100_test_target_categorical.csv",
        featuresInput = Mnist.featuresInput,
        featuresTarget = Mnist.featuresTarget,
        trainSampleCount = 600,
        testSampleCount = 100
    )

    object Model {
        object TensorFlow {
            const val BreastCancerWisconsin = "$tfModelDir/breast_cancer_wisconsin.tflite"
            const val FashionMnist = "$tfModelDir/fashion_mnist.tflite"
            const val Mnist = "$tfModelDir/mnist.tflite"
        }

        object Pytorch {
            const val BreastCancerWisconsin = "$ptModelDir/breast_cancer_wisconsin.ptl"
            const val FashionMnist = "$ptModelDir/fashion_mnist.ptl"
            const val Mnist = "$ptModelDir/mnist.ptl"
        }
    }
}