package com.example.mobiledev.data.mock

enum class ActivityMetricType {
    AUTH,
    REQUEST,
    ASSIGNMENT,
    EN_ROUTE,
    ARRIVAL,
    COMPLETION,
    STAFF,
    ACCOUNT
}

data class ActivityMiniStatMock(
    val label: String,
    val value: String
)

data class ActivitySummaryMock(
    val title: String,
    val description: String,
    val value: String,
    val period: String,
    val type: ActivityMetricType
)

object MockActivityData {
    val miniStats: List<ActivityMiniStatMock> = listOf(
        ActivityMiniStatMock(label = "Today Actions", value = "252"),
        ActivityMiniStatMock(label = "Active Dispatch", value = "9"),
        ActivityMiniStatMock(label = "Open Requests", value = "12")
    )

    val summaries: List<ActivitySummaryMock> = listOf(
        ActivitySummaryMock(
            title = "Successful Sign-ins",
            description = "Users authenticated and reached dashboard.",
            value = "126",
            period = "Today",
            type = ActivityMetricType.AUTH
        ),
        ActivitySummaryMock(
            title = "Emergency Requests Created",
            description = "New ambulance support requests submitted.",
            value = "34",
            period = "Last 24h",
            type = ActivityMetricType.REQUEST
        ),
        ActivitySummaryMock(
            title = "Assignments Completed",
            description = "Pending requests assigned to ambulances.",
            value = "27",
            period = "Last 24h",
            type = ActivityMetricType.ASSIGNMENT
        ),
        ActivitySummaryMock(
            title = "Ambulances En Route",
            description = "Live active dispatches currently moving.",
            value = "9",
            period = "Now",
            type = ActivityMetricType.EN_ROUTE
        ),
        ActivitySummaryMock(
            title = "Arrivals Confirmed",
            description = "Cases where ambulance reached patient.",
            value = "21",
            period = "Today",
            type = ActivityMetricType.ARRIVAL
        ),
        ActivitySummaryMock(
            title = "Cases Completed",
            description = "Emergency workflow completed end-to-end.",
            value = "18",
            period = "Today",
            type = ActivityMetricType.COMPLETION
        ),
        ActivitySummaryMock(
            title = "Staff Records Updated",
            description = "Staff profiles created or edited.",
            value = "7",
            period = "This week",
            type = ActivityMetricType.STAFF
        ),
        ActivitySummaryMock(
            title = "Account Actions",
            description = "Profile and account management events.",
            value = "15",
            period = "This week",
            type = ActivityMetricType.ACCOUNT
        ),
        ActivitySummaryMock(
            title = "Vehicle Maintenance",
            description = "Routine checks performed on fleet.",
            value = "4",
            period = "This week",
            type = ActivityMetricType.EN_ROUTE
        ),
        ActivitySummaryMock(
            title = "Training Sessions",
            description = "First responder workshops completed.",
            value = "2",
            period = "This month",
            type = ActivityMetricType.STAFF
        ),
        ActivitySummaryMock(
            title = "System Updates",
            description = "Security patches applied to dashboard.",
            value = "1",
            period = "Yesterday",
            type = ActivityMetricType.ACCOUNT
        ),
        ActivitySummaryMock(
            title = "Inventory Alerts",
            description = "Medical supply levels checked.",
            value = "5",
            period = "Today",
            type = ActivityMetricType.REQUEST
        )
    )
}
