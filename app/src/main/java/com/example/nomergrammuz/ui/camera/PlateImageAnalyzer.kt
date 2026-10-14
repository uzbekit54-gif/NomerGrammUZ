package com.example.nomergrammuz.ui.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.nomergrammuz.domain.model.PlateFormatType
import com.example.nomergrammuz.domain.parser.PlateParserEngine
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class PlateImageAnalyzer(
    private val parserEngine: PlateParserEngine,
    private val onPlateDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var isProcessing = false
    private var lastDetectedPlate: String? = null
    private var lastDetectionTime = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || isProcessing) {
            imageProxy.close()
            return
        }

        isProcessing = true
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val currentTime = System.currentTimeMillis()
                var detectedResult: String? = null

                // Iterate through text blocks and build smart candidate list (single lines, combined adjacent lines, whole block)
                for (block in visionText.textBlocks) {
                    val candidates = mutableListOf<String>()

                    // 1. Single lines (cleaned of noise like pipes, quotes, bullets)
                    val cleanedLines = block.lines.map { line ->
                        line.text.replace(Regex("[|/\\\\_—•]"), " ").trim()
                    }.filter { it.isNotBlank() }

                    candidates.addAll(cleanedLines)

                    // 2. Adjacent pairs of lines (crucial for 2-row plates & separated region boxes: 01 + H 123456, CMD + 0701)
                    for (i in 0 until cleanedLines.size - 1) {
                        candidates.add("${cleanedLines[i]} ${cleanedLines[i + 1]}")
                    }

                    // 3. Whole block text collapsed
                    if (cleanedLines.size > 2) {
                        candidates.add(cleanedLines.joinToString(" "))
                    }

                    for (rawCandidate in candidates) {
                        val candidate = rawCandidate.trim()
                        if (candidate.length in 2..20) {
                            val result = parserEngine.parse(candidate)
                            if (result.isIdentified && result.formatType != PlateFormatType.UNKNOWN) {
                                detectedResult = result.formattedPlate
                                break
                            }
                        }
                    }

                    if (detectedResult != null) break
                }

                detectedResult?.let { plate ->
                    if (plate != lastDetectedPlate || currentTime - lastDetectionTime > 1500) {
                        lastDetectedPlate = plate
                        lastDetectionTime = currentTime
                        onPlateDetected(plate)
                    }
                }
            }
            .addOnFailureListener {
                // Ignore transient frame analysis failures
            }
            .addOnCompleteListener {
                isProcessing = false
                imageProxy.close()
            }
    }
}
