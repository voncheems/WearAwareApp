package com.example.wearawarer

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.wearawarer.databinding.ActivityQuizBinding

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding

    private val quizBank: Map<String, List<QuizQuestion>> = mapOf(
        "HARD_HAT" to listOf(
            QuizQuestion("When must a hard hat be worn on a construction site?",
                listOf("Only when working at heights", "At all times in designated hard hat zones", "Only during heavy machinery operations", "Only when a supervisor is present"), 1),
            QuizQuestion("How often should a hard hat be inspected for damage?",
                listOf("Once a year", "Only after an impact", "Before each use", "Every month"), 2),
            QuizQuestion("Which of the following voids a hard hat's protection?",
                listOf("Writing your name on it", "Drilling holes for ventilation", "Wearing it with the brim forward", "Painting it a different color"), 1),
            QuizQuestion("What is the maximum lifespan of a hard hat shell from date of manufacture?",
                listOf("2 years", "5 years", "10 years", "It never expires"), 2),
            QuizQuestion("Which hard hat class offers protection against high-voltage electrical hazards?",
                listOf("Class A", "Class B", "Class C", "Class G"), 1)
        ),
        "SAFETY_VEST" to listOf(
            QuizQuestion("What is the primary purpose of a high-visibility safety vest?",
                listOf("To keep workers warm", "To make workers visible to vehicles and machinery", "To identify team leaders", "To carry tools"), 1),
            QuizQuestion("Which ANSI class vest is required for highway construction workers?",
                listOf("Class 1", "Class 2", "Class 3", "Class 4"), 2),
            QuizQuestion("When should a safety vest be replaced?",
                listOf("Every year regardless of condition", "When reflective strips lose visibility or fabric is damaged", "Only when a manager requests it", "After 100 washes"), 1),
            QuizQuestion("At night or in low light, which vest type provides the highest visibility?",
                listOf("Class 1 with yellow fabric", "Class 2 with orange fabric", "Class 3 with both fluorescent and retroreflective material", "Any vest with a single reflective strip"), 2),
            QuizQuestion("Can a safety vest be worn underneath a jacket and still be compliant?",
                listOf("Yes, always", "No, it must always be the outermost garment", "Only if the jacket is also high-visibility", "Only during daytime"), 1)
        ),
        "GLOVES" to listOf(
            QuizQuestion("Which glove type is appropriate for handling sharp metal objects?",
                listOf("Latex gloves", "Cut-resistant gloves", "Cotton gloves", "Rubber gloves"), 1),
            QuizQuestion("Should gloves be worn while operating rotating machinery like drills?",
                listOf("Yes, always for hand protection", "No, gloves can get caught in rotating parts", "Only thick leather gloves are acceptable", "Only fingerless gloves"), 1),
            QuizQuestion("What does an EN388 rating on gloves indicate?",
                listOf("Thermal resistance", "Chemical resistance", "Mechanical protection level", "Electrical insulation"), 2),
            QuizQuestion("How should chemical-resistant gloves be removed to avoid contamination?",
                listOf("Pull from the fingertips", "Peel from the wrist inward without touching the outer surface", "Shake them off", "Have a coworker remove them"), 1),
            QuizQuestion("Which gloves are required when working with electrical wiring?",
                listOf("Cut-resistant gloves", "Latex disposable gloves", "Insulated rubber gloves rated for the voltage", "Heavy leather gloves"), 2)
        ),
        "BOOTS" to listOf(
            QuizQuestion("What is the minimum steel toe rating required for general construction work?",
                listOf("ASTM F2412 impact resistance 50 joules", "ASTM F2413 with I/75 C/75 rating", "Any closed-toe shoe is acceptable", "Only leather boots are allowed"), 1),
            QuizQuestion("What additional feature is required for boots in areas with electrical hazards?",
                listOf("Steel toe cap", "EH (Electrical Hazard) rating", "Waterproofing", "Side zipper"), 1),
            QuizQuestion("How often should safety boots be inspected?",
                listOf("Once a year", "Only when they look damaged", "Before each use", "Every 6 months"), 2),
            QuizQuestion("Which boot sole feature is most important in oily or wet environments?",
                listOf("Steel midsole", "Slip-resistant outsole", "Insulated lining", "Composite toe"), 1),
            QuizQuestion("When should safety boots be immediately replaced?",
                listOf("After 1 year of use", "When the steel toe is exposed or sole is separating", "When they get wet", "After working in mud"), 1)
        ),
        "EYE_PROTECTION" to listOf(
            QuizQuestion("Which type of eye protection should be used when grinding metal?",
                listOf("Regular prescription glasses", "Safety spectacles only", "Goggles or face shield rated for impact", "Sunglasses"), 2),
            QuizQuestion("What ANSI standard governs eye and face protection in the workplace?",
                listOf("ANSI Z87.1", "ANSI Z41", "ANSI Z89.1", "ANSI Z358.1"), 0),
            QuizQuestion("When working with chemicals, which eye protection is most appropriate?",
                listOf("Safety spectacles", "Indirect-vent chemical splash goggles", "Face shield alone", "Tinted safety glasses"), 1),
            QuizQuestion("Can a face shield replace safety goggles as primary eye protection?",
                listOf("Yes, always", "No, a face shield is secondary protection only", "Only for chemical splash", "Only indoors"), 1),
            QuizQuestion("How should fogged safety goggles be cleaned on site?",
                listOf("Wipe with shirt or rag", "Use anti-fog spray or approved lens wipes", "Dip in water", "Leave them to clear naturally"), 1)
        ),
        "RESPIRATORY" to listOf(
            QuizQuestion("Which respirator type is effective against fine dust particles?",
                listOf("N95 filtering facepiece respirator", "Simple surgical mask", "Cloth mask", "Bandana"), 0),
            QuizQuestion("What does the 'N' in N95 respirator stand for?",
                listOf("Nanoparticle filter", "Not resistant to oil-based particles", "Nitrogen-rated", "National standard"), 1),
            QuizQuestion("Before wearing a tight-fitting respirator, what must be performed?",
                listOf("A medical evaluation and fit test", "A visual inspection only", "A smell test", "No special requirement"), 0),
            QuizQuestion("When should disposable N95 respirators be discarded?",
                listOf("After 30 days", "When damaged, soiled, or breathing resistance increases", "Only when visibly dirty", "After each shift"), 1),
            QuizQuestion("Which respirator is required when working with paint fumes containing solvents?",
                listOf("N95 particulate respirator", "Half-face respirator with organic vapor cartridges", "Simple dust mask", "No respirator needed"), 1)
        )
    )

    private var currentQuestions: List<QuizQuestion> = emptyList()
    private var currentIndex = 0
    private var score = 0
    private var answered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val moduleKey = intent.getStringExtra("MODULE_KEY") ?: "HARD_HAT"
        val moduleTitle = intent.getStringExtra("MODULE_TITLE") ?: "Training"

        binding.tvModuleTitle.text = moduleTitle.uppercase()
        currentQuestions = quizBank[moduleKey] ?: emptyList()

        binding.ivBack.setOnClickListener { finish() }
        setupOptionClicks()
        loadQuestion()
    }

    private fun setupOptionClicks() {
        binding.optionA.setOnClickListener { checkAnswer(0) }
        binding.optionB.setOnClickListener { checkAnswer(1) }
        binding.optionC.setOnClickListener { checkAnswer(2) }
        binding.optionD.setOnClickListener { checkAnswer(3) }
        binding.btnNext.setOnClickListener { nextQuestion() }
    }

    private fun loadQuestion() {
        if (currentIndex >= currentQuestions.size) {
            showResults()
            return
        }

        answered = false
        val q = currentQuestions[currentIndex]

        binding.tvQuestionNumber.text = "Question ${currentIndex + 1} of ${currentQuestions.size}"
        binding.tvScore.text = "Score: $score"
        binding.tvQuestion.text = q.question
        binding.progressQuiz.progress = ((currentIndex.toFloat() / currentQuestions.size) * 100).toInt()

        val optionViews = listOf(binding.tvOptionA, binding.tvOptionB, binding.tvOptionC, binding.tvOptionD)
        val labelViews = listOf(binding.tvOptionALabel, binding.tvOptionBLabel, binding.tvOptionCLabel, binding.tvOptionDLabel)
        val cardViews = listOf(binding.optionA, binding.optionB, binding.optionC, binding.optionD)

        q.options.forEachIndexed { i, option ->
            optionViews[i].text = option
            labelViews[i].setTextColor(Color.parseColor("#0F2849"))
            cardViews[i].setCardBackgroundColor(Color.WHITE)
            cardViews[i].isClickable = true
        }

        binding.tvFeedback.visibility = View.GONE
        binding.btnNext.visibility = View.GONE
    }

    private fun checkAnswer(selectedIndex: Int) {
        if (answered) return
        answered = true

        val q = currentQuestions[currentIndex]
        val cardViews = listOf(binding.optionA, binding.optionB, binding.optionC, binding.optionD)
        val labelViews = listOf(binding.tvOptionALabel, binding.tvOptionBLabel, binding.tvOptionCLabel, binding.tvOptionDLabel)

        cardViews.forEach { it.isClickable = false }

        if (selectedIndex == q.correctIndex) {
            score++
            cardViews[selectedIndex].setCardBackgroundColor(Color.parseColor("#D1FAE5"))
            labelViews[selectedIndex].setTextColor(Color.parseColor("#10B981"))
            binding.tvFeedback.text = "✓ Correct!"
            binding.tvFeedback.setTextColor(Color.parseColor("#10B981"))
        } else {
            cardViews[selectedIndex].setCardBackgroundColor(Color.parseColor("#FEE2E2"))
            labelViews[selectedIndex].setTextColor(Color.parseColor("#EF4444"))
            cardViews[q.correctIndex].setCardBackgroundColor(Color.parseColor("#D1FAE5"))
            labelViews[q.correctIndex].setTextColor(Color.parseColor("#10B981"))
            binding.tvFeedback.text = "✗ Incorrect. The correct answer is highlighted."
            binding.tvFeedback.setTextColor(Color.parseColor("#EF4444"))
        }

        binding.tvFeedback.visibility = View.VISIBLE
        binding.btnNext.visibility = View.VISIBLE

        val isLast = currentIndex == currentQuestions.size - 1
        val nextLabel = if (isLast) "See Results" else "Next Question"
        binding.btnNext.findViewById<android.widget.TextView>(
            binding.btnNext.getChildAt(0).id.takeIf { it != View.NO_ID }
                ?: android.R.id.text1
        )

        // Update button text
        (binding.btnNext as? CardView)?.let {
            val tv = it.getChildAt(0) as? android.widget.TextView
            tv?.text = nextLabel
        }
    }

    private fun nextQuestion() {
        currentIndex++
        if (currentIndex >= currentQuestions.size) {
            showResults()
        } else {
            loadQuestion()
        }
    }

    private fun showResults() {
        val total = currentQuestions.size
        val percent = (score.toFloat() / total * 100).toInt()
        val passed = percent >= 80

        binding.tvQuestion.text = if (passed)
            "🎉 You passed!\n\nYou scored $score out of $total ($percent%)\n\nGreat job on this module."
        else
            "Quiz Complete\n\nYou scored $score out of $total ($percent%)\n\nYou need 80% to pass. Review the material and try again."

        binding.tvQuestion.setTextColor(Color.WHITE)

        listOf(binding.optionA, binding.optionB, binding.optionC, binding.optionD).forEach {
            it.visibility = View.GONE
        }
        binding.tvFeedback.visibility = View.GONE
        binding.tvQuestionNumber.text = "Results"
        binding.tvScore.text = "Score: $score/$total"
        binding.progressQuiz.progress = 100

        (binding.btnNext as? CardView)?.let {
            val tv = it.getChildAt(0) as? android.widget.TextView
            tv?.text = "Back to Training"
        }
        binding.btnNext.visibility = View.VISIBLE
        binding.btnNext.setOnClickListener { finish() }
    }
}