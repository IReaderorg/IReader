# Funding Goals Usage Guide

## For Developers

### Fetching Funding Goals

```kotlin
// In a ViewModel or use case
class MyViewModel(
    private val getFundingGoalsUseCase: GetFundingGoalsUseCase
) : ViewModel() {
    
    fun loadGoals() {
        viewModelScope.launch {
            val goals = getFundingGoalsUseCase()
            // Use goals in UI
        }
    }
}
```

### Updating Goal Progress (Admin Only)

```kotlin
// In admin panel or developer tools
class AdminViewModel(
    private val updateFundingGoalUseCase: UpdateFundingGoalUseCase
) : ViewModel() {
    
    fun updateGoalProgress(goalId: String, newAmount: Double) {
        viewModelScope.launch {
            val result = updateFundingGoalUseCase(goalId, newAmount)
            result.onSuccess { updatedGoal ->
                println("Goal updated: ${updatedGoal.title}")
            }.onFailure { error ->
                println("Failed to update goal: ${error.message}")
            }
        }
    }
    
    fun rolloverMonthlyGoal(goalId: String) {
        viewModelScope.launch {
            val result = updateFundingGoalUseCase.rolloverRecurringGoal(goalId)
            result.onSuccess { newGoal ->
                println("Goal rolled over for new month")
            }
        }
    }
}
```

### Creating Custom Goals

```kotlin
// In repository implementation
val customGoal = FundingGoal(
    id = "custom_feature_2024_11",
    title = "Dark Mode Themes Pack",
    description = "Fund the development of 10 new dark mode themes " +
                  "with custom color schemes and gradients.",
    targetAmount = 250.0,
    currentAmount = 0.0,
    currency = "USD",
    isRecurring = false
)

repository.createGoal(customGoal)
```

## For UI Integration

### Basic Usage

```kotlin
@Composable
fun MyDonationScreen() {
    val viewModel = getScreenModel<DonationViewModel>()
    val state by viewModel.state.collectAsState()
    
    Column {
        // Other donation content...
        
        FundaFeatureSection(
            fundingGoals = state.fundingGoals
        )
    }
}
```

### Custom Styling

```kotlin
@Composable
fun CustomFundaFeatureSection() {
    val goals = remember { /* get goals */ }
    
    FundaFeatureSection(
        fundingGoals = goals,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
```

## For Remote Config Integration

### Firebase Remote Config

1. Add Firebase dependency to your project
2. Create a JSON structure for goals:

```json
{
  "funding_goals": [
    {
      "id": "monthly_server_costs",
      "title": "Monthly Server Costs",
      "description": "Cover our server bills...",
      "targetAmount": 100.0,
      "currentAmount": 50.0,
      "currency": "USD",
      "isRecurring": true
    }
  ]
}
```

3. Implement Firebase repository:

```kotlin
class FirebaseFundingGoalRepository(
    private val remoteConfig: FirebaseRemoteConfig
) : FundingGoalRepository {
    
    override suspend fun getFundingGoals(): Result<List<FundingGoal>> {
        return try {
            remoteConfig.fetchAndActivate().await()
            val json = remoteConfig.getString("funding_goals")
            val goals = Json.decodeFromString<List<FundingGoal>>(json)
            Result.success(goals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

4. Update DI module:

```kotlin
single<FundingGoalRepository> { 
    FirebaseFundingGoalRepository(get()) 
}
```

### Supabase Integration

1. Create a `funding_goals` table:

```sql
CREATE TABLE funding_goals (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    target_amount DECIMAL(10, 2) NOT NULL,
    current_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    currency TEXT NOT NULL DEFAULT 'USD',
    is_recurring BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

2. Implement Supabase repository:

```kotlin
class SupabaseFundingGoalRepository(
    private val supabase: SupabaseClient
) : FundingGoalRepository {
    
    override suspend fun getFundingGoals(): Result<List<FundingGoal>> {
        return try {
            val goals = supabase
                .from("funding_goals")
                .select()
                .decodeList<FundingGoal>()
            Result.success(goals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateGoalProgress(
        goalId: String,
        newAmount: Double
    ): Result<FundingGoal> {
        return try {
            supabase
                .from("funding_goals")
                .update({
                    set("current_amount", newAmount)
                    set("updated_at", "NOW()")
                }) {
                    filter { eq("id", goalId) }
                }
            
            // Fetch updated goal
            val updated = supabase
                .from("funding_goals")
                .select {
                    filter { eq("id", goalId) }
                }
                .decodeSingle<FundingGoal>()
            
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Scheduled Tasks

### Monthly Rollover (Cron Job)

```kotlin
// Run this at the start of each month (e.g., via WorkManager or cron)
class MonthlyGoalRolloverWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: FundingGoalRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val goals = repository.getFundingGoals().getOrNull() ?: return Result.failure()
            
            goals.filter { it.isRecurring && it.isReached }.forEach { goal ->
                repository.rolloverRecurringGoal(goal.id)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

// Schedule the worker
val rolloverRequest = PeriodicWorkRequestBuilder<MonthlyGoalRolloverWorker>(
    repeatInterval = 30,
    repeatIntervalTimeUnit = TimeUnit.DAYS
).build()

WorkManager.getInstance(context).enqueue(rolloverRequest)
```

## Analytics

### Track Goal Views

```kotlin
// When user views funding goals
analytics.logEvent("funding_goals_viewed") {
    param("goal_count", goals.size)
}
```

### Track Goal Interactions

```kotlin
// When user taps a goal for details
analytics.logEvent("funding_goal_clicked") {
    param("goal_id", goal.id)
    param("goal_title", goal.title)
    param("progress_percent", goal.progressPercent)
}
```

### Track Goal Completion

```kotlin
// When a goal is reached
analytics.logEvent("funding_goal_reached") {
    param("goal_id", goal.id)
    param("goal_title", goal.title)
    param("target_amount", goal.targetAmount)
}
```

## Testing

### Mock Repository for Tests

```kotlin
class MockFundingGoalRepository : FundingGoalRepository {
    private val goals = mutableListOf<FundingGoal>()
    
    override suspend fun getFundingGoals(): Result<List<FundingGoal>> {
        return Result.success(goals.toList())
    }
    
    override suspend fun updateGoalProgress(
        goalId: String,
        newAmount: Double
    ): Result<FundingGoal> {
        val goal = goals.find { it.id == goalId }
            ?: return Result.failure(IllegalArgumentException("Goal not found"))
        
        val updated = goal.copy(currentAmount = newAmount)
        goals[goals.indexOf(goal)] = updated
        return Result.success(updated)
    }
    
    fun addTestGoal(goal: FundingGoal) {
        goals.add(goal)
    }
}
```

### UI Tests

```kotlin
@Test
fun fundingGoals_displayCorrectly() {
    val testGoals = listOf(
        FundingGoal(
            id = "test1",
            title = "Test Goal 1",
            description = "Description 1",
            targetAmount = 100.0,
            currentAmount = 50.0
        ),
        FundingGoal(
            id = "test2",
            title = "Test Goal 2",
            description = "Description 2",
            targetAmount = 200.0,
            currentAmount = 200.0
        )
    )
    
    composeTestRule.setContent {
        FundaFeatureSection(fundingGoals = testGoals)
    }
    
    // Verify first goal
    composeTestRule.onNodeWithText("Test Goal 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("50%").assertIsDisplayed()
    
    // Verify second goal (reached)
    composeTestRule.onNodeWithText("Test Goal 2").assertIsDisplayed()
    composeTestRule.onNodeWithText("Goal Reached!").assertIsDisplayed()
}
```

## Best Practices

1. **Cache Goals**: Cache funding goals locally to reduce API calls
2. **Refresh Periodically**: Update goals every few hours or on app launch
3. **Handle Errors Gracefully**: Show cached goals if API fails
4. **Validate Amounts**: Always validate amounts before updating
5. **Secure Admin Access**: Protect update endpoints with authentication
6. **Monitor Progress**: Track goal progress with analytics
7. **Notify Users**: Send push notifications when goals are reached
8. **Archive Old Goals**: Keep history of completed goals

## Troubleshooting

### Goals Not Showing

1. Check if repository is returning goals
2. Verify DI setup is correct
3. Check ViewModel is loading goals
4. Ensure UI is observing state correctly

### Progress Not Updating

1. Verify repository update method is called
2. Check if state is being updated in ViewModel
3. Ensure UI is recomposing on state changes
4. Check for caching issues

### Rollover Not Working

1. Verify goal is marked as recurring
2. Check if rollover logic is triggered
3. Ensure new goal is created correctly
4. Verify old goal is archived

## Support

For issues or questions:
- Check the FUND_A_FEATURE_README.md
- Review the implementation in the codebase
- Contact the development team
- Open an issue on GitHub
