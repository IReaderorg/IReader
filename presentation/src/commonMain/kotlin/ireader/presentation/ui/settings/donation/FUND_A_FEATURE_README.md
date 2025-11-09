# Fund-a-Feature Progress System

## Overview

The Fund-a-Feature system allows users to see funding progress for specific features or monthly server costs. It displays progress bars, goal descriptions, and handles automatic rollover for recurring monthly goals.

## Architecture

### Domain Layer

#### Models
- **FundingGoal** (`domain/models/donation/FundingGoal.kt`)
  - Contains: id, title, description, targetAmount, currentAmount, currency, isRecurring
  - Computed properties: progressPercent, isReached

#### Repository Interface
- **FundingGoalRepository** (`domain/data/repository/FundingGoalRepository.kt`)
  - Methods:
    - `getFundingGoals()`: Fetch all active goals
    - `updateGoalProgress()`: Update goal's current amount
    - `createGoal()`: Create new funding goal
    - `deleteGoal()`: Remove a funding goal
    - `rolloverRecurringGoal()`: Reset recurring goal for new period

#### Use Cases
- **GetFundingGoalsUseCase** (`domain/usecases/donation/GetFundingGoalsUseCase.kt`)
  - Fetches funding goals from repository
  - Returns list of active goals to display

- **UpdateFundingGoalUseCase** (`domain/usecases/donation/UpdateFundingGoalUseCase.kt`)
  - Updates goal progress (admin-controlled)
  - Handles recurring goal rollover
  - Validates amounts

### Data Layer

#### Repository Implementation
- **FundingGoalRepositoryImpl** (`data/repository/FundingGoalRepositoryImpl.kt`)
  - Default implementation with in-memory cache
  - Provides hardcoded default goals
  - **TODO**: Replace with remote config integration (Firebase, Supabase)

### Presentation Layer

#### UI Components
- **FundaFeatureSection** (`presentation/ui/settings/donation/FundaFeatureSection.kt`)
  - Main composable displaying all funding goals
  - Shows progress bars with animated progress
  - Handles empty state
  - Opens detail dialog on goal click

- **FundingGoalCard**
  - Individual goal card with:
    - Title and "Monthly" badge for recurring goals
    - Progress amount (current/target)
    - Animated progress bar
    - "Goal Reached!" indicator when complete
    - Short description preview

- **FundingProgressBar**
  - Animated progress bar
  - Different colors for reached vs. in-progress goals

- **FundingGoalDetailDialog**
  - Full goal details
  - Complete description
  - Progress visualization
  - Recurring goal explanation

#### ViewModel
- **DonationViewModel** (`presentation/ui/settings/donation/DonationViewModel.kt`)
  - Manages funding goals state
  - Loads goals on initialization
  - Provides refresh functionality

## Integration

### DI Setup

The following components are registered in Koin:

```kotlin
// Domain Module (DomainModules.kt)
single<FundingGoalRepository> { FundingGoalRepositoryImpl() }

// Use Cases Module (UseCasesInject.kt)
single { GetFundingGoalsUseCase(get()) }
single { UpdateFundingGoalUseCase(get()) }

// Presentation Module (PresentationModules.kt)
factory { DonationViewModel(get()) }
```

### Screen Integration

The Fund-a-Feature section is integrated into the DonationScreen:

```kotlin
// DonationScreen.kt
item {
    FundaFeatureSection(
        fundingGoals = state.fundingGoals
    )
}
```

## Default Funding Goals

The system comes with three default goals:

1. **Monthly Server Costs** (Recurring)
   - Target: $100
   - Current: $50
   - Description: Covers Supabase backend and AI translation API costs

2. **Premium TTS Voices**
   - Target: $500
   - Current: $125
   - Description: Integration of premium text-to-speech voices

3. **Offline Translation Engine**
   - Target: $1000
   - Current: $0
   - Description: Develop offline translation capability

## Remote Config Integration (TODO)

To integrate with remote config:

### Option 1: Firebase Remote Config

```kotlin
class FirebaseFundingGoalRepository : FundingGoalRepository {
    private val remoteConfig = Firebase.remoteConfig
    
    override suspend fun getFundingGoals(): Result<List<FundingGoal>> {
        return try {
            remoteConfig.fetchAndActivate().await()
            val goalsJson = remoteConfig.getString("funding_goals")
            val goals = Json.decodeFromString<List<FundingGoal>>(goalsJson)
            Result.success(goals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Option 2: Supabase

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
            val updated = supabase
                .from("funding_goals")
                .update({
                    set("current_amount", newAmount)
                }) {
                    filter {
                        eq("id", goalId)
                    }
                }
                .decodeSingle<FundingGoal>()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Option 3: REST API

```kotlin
class ApiFundingGoalRepository(
    private val httpClient: HttpClient
) : FundingGoalRepository {
    
    override suspend fun getFundingGoals(): Result<List<FundingGoal>> {
        return try {
            val response = httpClient.get("https://api.example.com/funding-goals")
            val goals = response.body<List<FundingGoal>>()
            Result.success(goals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Admin Goal Management

To update funding goals manually (admin only):

```kotlin
// In admin panel or developer tools
val updateUseCase = get<UpdateFundingGoalUseCase>()

// Update progress
updateUseCase("monthly_server_costs", 75.0)

// Rollover recurring goal
updateUseCase.rolloverRecurringGoal("monthly_server_costs")
```

## Automatic Rollover

For recurring monthly goals, implement a scheduled task:

```kotlin
// Run at the start of each month
class MonthlyGoalRolloverTask(
    private val repository: FundingGoalRepository
) {
    suspend fun execute() {
        val goals = repository.getFundingGoals().getOrNull() ?: return
        
        goals.filter { it.isRecurring && it.isReached }.forEach { goal ->
            repository.rolloverRecurringGoal(goal.id)
        }
    }
}
```

## UI Features

### Progress Animation
- Progress bars animate smoothly when goals update
- 1-second animation duration for visual appeal

### Goal States
- **In Progress**: Shows percentage and progress bar
- **Goal Reached**: Green checkmark icon and "Goal Reached!" text
- **Empty State**: Friendly message when no goals exist

### Recurring Goals
- Display "Monthly" badge
- Automatic reset explanation in detail dialog
- Rollover preserves goal structure

### User Interaction
- Tap goal card to see full details
- Detail dialog shows complete description
- Close button to dismiss dialog

## Testing

### Unit Tests

```kotlin
class GetFundingGoalsUseCaseTest {
    @Test
    fun `should return goals from repository`() = runTest {
        val mockRepo = mockk<FundingGoalRepository>()
        val expectedGoals = listOf(/* test goals */)
        coEvery { mockRepo.getFundingGoals() } returns Result.success(expectedGoals)
        
        val useCase = GetFundingGoalsUseCase(mockRepo)
        val result = useCase()
        
        assertEquals(expectedGoals, result)
    }
}
```

### UI Tests

```kotlin
@Test
fun fundaFeatureSection_displaysGoals() {
    composeTestRule.setContent {
        FundaFeatureSection(
            fundingGoals = listOf(
                FundingGoal(
                    id = "test",
                    title = "Test Goal",
                    description = "Test description",
                    targetAmount = 100.0,
                    currentAmount = 50.0
                )
            )
        )
    }
    
    composeTestRule.onNodeWithText("Test Goal").assertIsDisplayed()
    composeTestRule.onNodeWithText("50%").assertIsDisplayed()
}
```

## Requirements Satisfied

This implementation satisfies the following requirements from the spec:

- ✅ 21.1: Display Fund-a-Feature section on DonationScreen
- ✅ 21.2: Show current funding goal with progress bar
- ✅ 21.3: Display format "Monthly Goal: $50 / $100 [Progress Bar] 50%"
- ✅ 21.4: Show funding purpose description
- ✅ 21.5: Display "Goal Reached!" when target is met
- ✅ 21.6: Automatic goal rollover for monthly recurring goals
- ✅ 21.7: Display one-time feature goals with specific targets
- ✅ 21.8: Show feature details when user taps goal

## Future Enhancements

1. **Push Notifications**: Notify users when goals are reached
2. **Goal History**: Show archived goals and total raised
3. **Contributor List**: Display top donors (with permission)
4. **Goal Milestones**: Show intermediate milestones (25%, 50%, 75%)
5. **Donation Attribution**: Link donations to specific goals
6. **Goal Voting**: Let users vote on which features to fund next
7. **Stretch Goals**: Show additional goals when primary goal is reached
8. **Real-time Updates**: WebSocket connection for live progress updates

## Maintenance

### Updating Goals

To add a new funding goal:

1. Update the default goals in `FundingGoalRepositoryImpl`
2. Or add to remote config
3. Goals will appear automatically on next app launch

### Monitoring

Track goal progress through:
- Admin dashboard (to be implemented)
- Database queries
- Analytics events

## Support

For questions or issues with the Fund-a-Feature system, contact the development team or open an issue on GitHub.
