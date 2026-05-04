const HOSPITALS = [
  ["HOSPITAL_1", "ADMIN_1", "Mulago National Referral Hospital", "hospital1@resq.local", "+256 414 554001", "Mulago Hill, Kampala", 0.3381, 32.5761, "UUID_HOSP_1", "APPROVED", 3],
  ["HOSPITAL_2", "ADMIN_2", "Nakasero Hospital", "hospital2@resq.local", "+256 414 346150", "Akwright Projects, Kampala", 0.3236, 32.5794, "UUID_HOSP_2", "APPROVED", 2],
  ["HOSPITAL_3", "ADMIN_3", "International Hospital Kampala", "hospital3@resq.local", "+256 312 200400", "Namuwongo Road, Kampala", 0.3044, 32.6074, "UUID_HOSP_3", "APPROVED", 2],
  ["HOSPITAL_4", "ADMIN_4", "Lubaga Hospital", "hospital4@resq.local", "+256 414 270222", "Lubaga Hill, Kampala", 0.3025, 32.5594, "UUID_HOSP_4", "APPROVED", 3],
  ["HOSPITAL_5", "ADMIN_5", "Mengo Hospital", "hospital5@resq.local", "+256 414 270222", "Mengo Hill, Kampala", 0.3130, 32.5580, "UUID_HOSP_5", "PENDING", 0],
  ["HOSPITAL_6", "ADMIN_6", "Case Medical Centre", "hospital6@resq.local", "+256 414 250362", "Buganda Road, Kampala", 0.3200, 32.5775, "UUID_HOSP_6", "APPROVED", 1],
  ["HOSPITAL_7", "ADMIN_7", "St. Francis Hospital Nsambya", "hospital7@resq.local", "+256 414 267012", "Nsambya Hill, Kampala", 0.3015, 32.5878, "UUID_HOSP_7", "REJECTED", 0],
  ["HOSPITAL_8", "ADMIN_8", "Norvik Hospital", "hospital8@resq.local", "+256 414 250362", "Bombo Road, Kampala", 0.3255, 32.5730, "UUID_HOSP_8", "APPROVED", 1],
  ["HOSPITAL_9", "ADMIN_9", "Kibuli Muslim Hospital", "hospital9@resq.local", "+256 414 235882", "Kibuli Hill, Kampala", 0.3090, 32.5960, "UUID_HOSP_9", "APPROVED", 1],
  ["HOSPITAL_10", "ADMIN_10", "Victoria Hospital", "hospital10@resq.local", "+256 414 341688", "Bukoto, Kampala", 0.3420, 32.5980, "UUID_HOSP_10", "APPROVED", 1]
];

const USERS = [
  ["ADMIN_1", "HOSPITAL_1", "Alice Admin", "admin1@resq.local", "+256 701 1001", "Mulago Hill", "HOSPITAL_ADMIN", "UUID_USER_ADMIN_1"],
  ["ADMIN_2", "HOSPITAL_2", "Brian Admin", "admin2@resq.local", "+256 701 1002", "Nakasero Hill", "HOSPITAL_ADMIN", "UUID_USER_ADMIN_2"],
  ["DRIVER_1", "HOSPITAL_1", "Derek Driver", "driver1@resq.local", "+256 701 1101", "Mulago Station", "DRIVER", "UUID_USER_DRIVER_1"],
  ["DRIVER_2", "HOSPITAL_1", "Diana Driver", "driver2@resq.local", "+256 701 1102", "Mulago Station", "DRIVER", "UUID_USER_DRIVER_2"],
  ["DRIVER_3", "HOSPITAL_2", "Evan Driver", "driver3@resq.local", "+256 701 1103", "Nakasero Station", "DRIVER", "UUID_USER_DRIVER_3"],
  ["DRIVER_4", "HOSPITAL_2", "Emma Driver", "driver4@resq.local", "+256 701 1104", "Nakasero Station", "DRIVER", "UUID_USER_DRIVER_4"],
  ["PATIENT_1", null, "Paul Patient", "patient1@resq.local", "+256 701 1201", "Old Kampala", "PATIENT", "UUID_USER_PATIENT_1"],
  ["PATIENT_2", null, "Priya Patient", "patient2@resq.local", "+256 701 1202", "Kololo", "PATIENT", "UUID_USER_PATIENT_2"],
  ["PATIENT_3", null, "Peter Patient", "patient3@resq.local", "+256 701 1203", "Kireka", "PATIENT", "UUID_USER_PATIENT_3"],
  ["PATIENT_4", null, "Pam Patient", "patient4@resq.local", "+256 701 1204", "Bweyogerere", "PATIENT", "UUID_USER_PATIENT_4"]
];

const AMBULANCES = [
  ["AMB_1", "HOSPITAL_1", "DRIVER_1", "UBA 123A", "LIC-1001", "AVAILABLE", 0.3390, 32.5750],
  ["AMB_2", "HOSPITAL_1", "DRIVER_2", "UBA 456B", "LIC-1002", "ON_EMERGENCY", 0.3400, 32.5740],
  ["AMB_3", "HOSPITAL_2", "DRIVER_3", "UBB 789C", "LIC-2001", "AVAILABLE", 0.3240, 32.5800],
  ["AMB_4", "HOSPITAL_2", "DRIVER_4", "UBB 012D", "LIC-2002", "OFFLINE", 0.3250, 32.5780],
  ["AMB_5", "HOSPITAL_3", "DRIVER_1", "UBC 345E", "LIC-3001", "AVAILABLE", 0.3050, 32.6080],
  ["AMB_6", "HOSPITAL_3", "DRIVER_2", "UBC 678F", "LIC-3002", "AVAILABLE", 0.3060, 32.6060],
  ["AMB_7", "HOSPITAL_4", "DRIVER_3", "UBD 901G", "LIC-4001", "ON_EMERGENCY", 0.3030, 32.5600],
  ["AMB_8", "HOSPITAL_4", "DRIVER_4", "UBD 234H", "LIC-4002", "AVAILABLE", 0.3040, 32.5580],
  ["AMB_9", "HOSPITAL_1", "DRIVER_1", "UBA 789I", "LIC-1003", "OFFLINE", 0.3370, 32.5770],
  ["AMB_10", "HOSPITAL_2", "DRIVER_2", "UBB 345J", "LIC-2003", "AVAILABLE", 0.3230, 32.5810]
];

const REQUESTS = [
  ["REQ_1", "PATIENT_1", "HOSPITAL_1", null, "PENDING", "Severe chest pain", "Old Kampala Mosque", 0.3160, 32.5680, "CRITICAL", null],
  ["REQ_2", "PATIENT_2", "HOSPITAL_1", "AMB_2", "ASSIGNED", "Road traffic injury", "Independence Monument", 0.3150, 32.5815, "HIGH", 9],
  ["REQ_3", "PATIENT_3", "HOSPITAL_2", null, "PENDING", "Breathing difficulty", "Garden City Mall", 0.3215, 32.5910, "HIGH", null],
  ["REQ_4", "PATIENT_4", "HOSPITAL_2", "AMB_3", "ARRIVED", "Fall from stairs", "Acacia Mall", 0.3340, 32.5890, "MEDIUM", 6],
  ["REQ_5", "PATIENT_1", "HOSPITAL_3", "AMB_5", "ASSIGNED", "Suspected stroke", "Makerere University", 0.3345, 32.5670, "CRITICAL", 7],
  ["REQ_6", "PATIENT_2", "HOSPITAL_3", null, "PENDING", "Allergic reaction", "Kisementi", 0.3330, 32.5920, "MEDIUM", null],
  ["REQ_7", "PATIENT_3", "HOSPITAL_4", "AMB_7", "ON_WAY", "Burn injury", "Kamwokya", 0.3380, 32.5880, "HIGH", 5],
  ["REQ_8", "PATIENT_4", "HOSPITAL_4", "AMB_8", "COMPLETED", "Minor fracture", "Ntinda Complex", 0.3520, 32.6120, "LOW", 12],
  ["REQ_9", "PATIENT_1", "HOSPITAL_1", "AMB_1", "COMPLETED", "Fever and dehydration", "Bugolobi Market", 0.3180, 32.6170, "LOW", 10],
  ["REQ_10", "PATIENT_2", "HOSPITAL_2", null, "PENDING", "Unconscious person", "Kiwatule", 0.3550, 32.6240, "CRITICAL", null]
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
