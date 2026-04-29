const HOSPITALS = [
  ["HOSPITAL_1", "ADMIN_1", "City Central Hospital", "hospital1@resq.local", "555-0100", "123 Healthcare Ave", 40.7128, -74.0060, "UUID_HOSP_1", "APPROVED", 3],
  ["HOSPITAL_2", "ADMIN_2", "Northside Medical", "hospital2@resq.local", "555-0101", "78 North Street", 40.7306, -73.9352, "UUID_HOSP_2", "APPROVED", 2],
  ["HOSPITAL_3", "ADMIN_3", "West End General", "hospital3@resq.local", "555-0102", "91 West End Blvd", 40.7350, -74.0020, "UUID_HOSP_3", "APPROVED", 2],
  ["HOSPITAL_4", "ADMIN_4", "East River Clinic", "hospital4@resq.local", "555-0103", "14 River Road", 40.7210, -73.9800, "UUID_HOSP_4", "APPROVED", 3],
  ["HOSPITAL_5", "ADMIN_5", "South Metro Hospital", "hospital5@resq.local", "555-0104", "501 South Metro", 40.7001, -73.9901, "UUID_HOSP_5", "PENDING", 0],
  ["HOSPITAL_6", "ADMIN_6", "Lakeside Emergency", "hospital6@resq.local", "555-0105", "8 Lake View", 40.7450, -73.9700, "UUID_HOSP_6", "APPROVED", 1],
  ["HOSPITAL_7", "ADMIN_7", "Greenfield Health", "hospital7@resq.local", "555-0106", "300 Greenfield", 40.7421, -73.9550, "UUID_HOSP_7", "REJECTED", 0],
  ["HOSPITAL_8", "ADMIN_8", "Harborpoint Medical", "hospital8@resq.local", "555-0107", "22 Harbor St", 40.7105, -74.0150, "UUID_HOSP_8", "APPROVED", 1],
  ["HOSPITAL_9", "ADMIN_9", "Hillview Trauma", "hospital9@resq.local", "555-0108", "44 Hillview Lane", 40.7601, -73.9802, "UUID_HOSP_9", "APPROVED", 1],
  ["HOSPITAL_10", "ADMIN_10", "Sunrise Care Center", "hospital10@resq.local", "555-0109", "77 Sunrise Ave", 40.7511, -73.9402, "UUID_HOSP_10", "APPROVED", 1]
];

const USERS = [
  ["ADMIN_1", "HOSPITAL_1", "Alice Admin", "admin1@resq.local", "555-1001", "City Central Hospital", "HOSPITAL_ADMIN", "UUID_USER_ADMIN_1"],
  ["ADMIN_2", "HOSPITAL_2", "Brian Admin", "admin2@resq.local", "555-1002", "Northside Medical", "HOSPITAL_ADMIN", "UUID_USER_ADMIN_2"],
  ["DRIVER_1", "HOSPITAL_1", "Derek Driver", "driver1@resq.local", "555-1101", "Station A", "DRIVER", "UUID_USER_DRIVER_1"],
  ["DRIVER_2", "HOSPITAL_1", "Diana Driver", "driver2@resq.local", "555-1102", "Station A", "DRIVER", "UUID_USER_DRIVER_2"],
  ["DRIVER_3", "HOSPITAL_2", "Evan Driver", "driver3@resq.local", "555-1103", "Station B", "DRIVER", "UUID_USER_DRIVER_3"],
  ["DRIVER_4", "HOSPITAL_2", "Emma Driver", "driver4@resq.local", "555-1104", "Station B", "DRIVER", "UUID_USER_DRIVER_4"],
  ["PATIENT_1", null, "Paul Patient", "patient1@resq.local", "555-1201", "Times Square", "PATIENT", "UUID_USER_PATIENT_1"],
  ["PATIENT_2", null, "Priya Patient", "patient2@resq.local", "555-1202", "Central Park", "PATIENT", "UUID_USER_PATIENT_2"],
  ["PATIENT_3", null, "Peter Patient", "patient3@resq.local", "555-1203", "Wall Street", "PATIENT", "UUID_USER_PATIENT_3"],
  ["PATIENT_4", null, "Pam Patient", "patient4@resq.local", "555-1204", "Queens Blvd", "PATIENT", "UUID_USER_PATIENT_4"]
];

const AMBULANCES = [
  ["AMB_1", "HOSPITAL_1", "DRIVER_1", "REG-1001", "LIC-1001", "AVAILABLE", 40.7130, -74.0065],
  ["AMB_2", "HOSPITAL_1", "DRIVER_2", "REG-1002", "LIC-1002", "ON_EMERGENCY", 40.7140, -74.0055],
  ["AMB_3", "HOSPITAL_2", "DRIVER_3", "REG-2001", "LIC-2001", "AVAILABLE", 40.7301, -73.9360],
  ["AMB_4", "HOSPITAL_2", "DRIVER_4", "REG-2002", "LIC-2002", "OFFLINE", 40.7310, -73.9340],
  ["AMB_5", "HOSPITAL_3", "DRIVER_1", "REG-3001", "LIC-3001", "AVAILABLE", 40.7355, -74.0018],
  ["AMB_6", "HOSPITAL_3", "DRIVER_2", "REG-3002", "LIC-3002", "AVAILABLE", 40.7361, -74.0030],
  ["AMB_7", "HOSPITAL_4", "DRIVER_3", "REG-4001", "LIC-4001", "ON_EMERGENCY", 40.7215, -73.9810],
  ["AMB_8", "HOSPITAL_4", "DRIVER_4", "REG-4002", "LIC-4002", "AVAILABLE", 40.7220, -73.9795],
  ["AMB_9", "HOSPITAL_1", "DRIVER_1", "REG-1003", "LIC-1003", "OFFLINE", 40.7122, -74.0071],
  ["AMB_10", "HOSPITAL_2", "DRIVER_2", "REG-2003", "LIC-2003", "AVAILABLE", 40.7298, -73.9348]
];

const REQUESTS = [
  ["REQ_1", "PATIENT_1", "HOSPITAL_1", null, "PENDING", "Severe chest pain", "Times Square", 40.7580, -73.9855, "CRITICAL", null],
  ["REQ_2", "PATIENT_2", "HOSPITAL_1", "AMB_2", "ASSIGNED", "Road traffic injury", "8th Avenue", 40.7610, -73.9830, "HIGH", 9],
  ["REQ_3", "PATIENT_3", "HOSPITAL_2", null, "PENDING", "Breathing difficulty", "Wall Street", 40.7060, -74.0090, "HIGH", null],
  ["REQ_4", "PATIENT_4", "HOSPITAL_2", "AMB_3", "ARRIVED", "Fall from stairs", "Lexington Ave", 40.7510, -73.9730, "MEDIUM", 6],
  ["REQ_5", "PATIENT_1", "HOSPITAL_3", "AMB_5", "ASSIGNED", "Suspected stroke", "Broadway", 40.7240, -74.0000, "CRITICAL", 7],
  ["REQ_6", "PATIENT_2", "HOSPITAL_3", null, "PENDING", "Allergic reaction", "SoHo", 40.7230, -74.0025, "MEDIUM", null],
  ["REQ_7", "PATIENT_3", "HOSPITAL_4", "AMB_7", "ON_WAY", "Burn injury", "East Village", 40.7265, -73.9815, "HIGH", 5],
  ["REQ_8", "PATIENT_4", "HOSPITAL_4", "AMB_8", "COMPLETED", "Minor fracture", "Queens Blvd", 40.7440, -73.8900, "LOW", 12],
  ["REQ_9", "PATIENT_1", "HOSPITAL_1", "AMB_1", "COMPLETED", "Fever and dehydration", "5th Avenue", 40.7750, -73.9650, "LOW", 10],
  ["REQ_10", "PATIENT_2", "HOSPITAL_2", null, "PENDING", "Unconscious person", "Madison Ave", 40.7600, -73.9740, "CRITICAL", null]
];

const MINI_STATS = [
  { label: "Today Actions", value: "252" },
  { label: "Active Dispatch", value: "9" },
  { label: "Open Requests", value: "12" }
];

const ACTIVITY_SUMMARIES = [
  { title: "Successful Sign-ins", description: "Users authenticated and reached dashboard.", value: "126", period: "Today", type: "AUTH" },
  { title: "Emergency Requests Created", description: "New ambulance support requests submitted.", value: "34", period: "Last 24h", type: "REQUEST" },
  { title: "Assignments Completed", description: "Pending requests assigned to ambulances.", value: "27", period: "Last 24h", type: "ASSIGNMENT" },
  { title: "Ambulances En Route", description: "Live active dispatches currently moving.", value: "9", period: "Now", type: "EN_ROUTE" },
  { title: "Arrivals Confirmed", description: "Cases where ambulance reached patient.", value: "21", period: "Today", type: "ARRIVAL" },
  { title: "Cases Completed", description: "Emergency workflow completed end-to-end.", value: "18", period: "Today", type: "COMPLETION" },
  { title: "Staff Records Updated", description: "Staff profiles created or edited.", value: "7", period: "This week", type: "STAFF" },
  { title: "Account Actions", description: "Profile and account management events.", value: "15", period: "This week", type: "ACCOUNT" }
];

module.exports = {
  HOSPITALS,
  USERS,
  AMBULANCES,
  REQUESTS,
  MINI_STATS,
  ACTIVITY_SUMMARIES
};

