#!/usr/bin/env node

const admin = require("firebase-admin");
const bcrypt = require("bcryptjs");
const {
  HOSPITALS,
  USERS,
  AMBULANCES,
  REQUESTS,
  MINI_STATS,
  ACTIVITY_SUMMARIES
} = require("./mockSeed");

const DEFAULT_DEV_PASSWORD = process.env.DEV_USER_PASSWORD || "password123";
const dryRun = process.argv.includes("--dry-run");

function normalizeEmail(email) {
  return (email || "").trim().toLowerCase();
}

function normalizePhone(phone) {
  return (phone || "").replace(/\D/g, "");
}

function createUserRecord({
  id,
  name,
  email,
  phone,
  hospitalId = null,
  location = "Unknown",
  role,
  accountStatus = "ACTIVE",
  createdAt,
  updatedAt,
  uuid,
  passwordHash
}) {
  const resolvedRole = role || "PATIENT";
  return {
    id,
    name,
    email,
    phone,
    password: passwordHash,
    role: resolvedRole,
    userType: resolvedRole,
    hospitalId,
    location,
    accountStatus,
    emailKey: normalizeEmail(email),
    phoneKey: normalizePhone(phone),
    createdAt,
    updatedAt,
    uuid: uuid || null
  };
}

function buildSeedData(now, passwordHash) {
  const hospitals = {};
  const users = {};
  const ambulances = {};
  const emergencyRequests = {};

  HOSPITALS.forEach((hosp, index) => {
    const [id, adminId, name, email, phone, location, latitude, longitude, uuid, status, activeAmbulances] = hosp;
    hospitals[id] = {
      id,
      adminId,
      name,
      email,
      phone,
      location,
      latitude,
      longitude,
      uuid,
      passwordHash,
      status,
      activeAmbulances,
      createdAt: now - (900000 - index * 10000),
      updatedAt: now - (300000 - index * 10000)
    };
  });

  USERS.forEach((user, index) => {
    const [id, hospitalId, name, email, phone, location, userType, uuid] = user;
    const status = userType === "HOSPITAL_ADMIN"
      ? (hospitals[hospitalId]?.status || "PENDING")
      : "ACTIVE";

    users[id] = createUserRecord({
      id,
      hospitalId,
      name,
      email,
      phone,
      location,
      role: userType,
      accountStatus: status,
      createdAt: now - (800000 - index * 1000),
      updatedAt: now - (200000 - index * 1000),
      uuid,
      passwordHash
    });
  });

  const roleUsers = [
    {
      id: "SYSTEM_ADMIN_1",
      name: "Sasha System",
      email: "sysadmin@resq.local",
      phone: "555-9001",
      role: "SYSTEM_ADMIN",
      accountStatus: "ACTIVE",
      location: "HQ"
    },
    {
      id: "PATIENT_DEV_1",
      name: "Default Patient",
      email: "patient.dev@resq.local",
      phone: "555-9002",
      role: undefined,
      accountStatus: "ACTIVE",
      location: "Dev Zone"
    },
    {
      id: "DRIVER_DEV_1",
      name: "Driver Dev",
      email: "driver.dev@resq.local",
      phone: "555-9003",
      role: "DRIVER",
      accountStatus: "ACTIVE",
      location: "Ambulance Bay",
      hospitalId: "HOSPITAL_1"
    }
  ];

  roleUsers.forEach((entry, index) => {
    users[entry.id] = createUserRecord({
      ...entry,
      createdAt: now - 50000 + index,
      updatedAt: now - 50000 + index,
      passwordHash
    });
  });

  AMBULANCES.forEach((amb, index) => {
    const [id, hospitalId, driverId, registrationNo, licenseNo, status, latitude, longitude] = amb;
    ambulances[id] = {
      id,
      hospitalId,
      driverId,
      registrationNo,
      licenseNo,
      status,
      latitude,
      longitude,
      createdAt: now - (700000 - index * 1000),
      updatedAt: now - (180000 - index * 1000)
    };
  });

  REQUESTS.forEach((req, index) => {
    const [id, userId, hospitalId, ambulanceId, status, description, location, latitude, longitude, priority, estimatedTimeMins] = req;
    emergencyRequests[id] = {
      id,
      userId,
      hospitalId,
      ambulanceId,
      status,
      description,
      location,
      latitude,
      longitude,
      priority,
      estimatedTimeMins,
      createdAt: now - (600000 - index * 1000),
      updatedAt: now - (160000 - index * 1000),
      completedAt: status === "COMPLETED" ? now - (120000 - index * 1000) : null,
      isDeleted: false
    };
  });

  return {
    users,
    hospitals,
    ambulances,
    emergencyRequests,
    dashboardMetrics: {
      miniStats: MINI_STATS,
      summaries: ACTIVITY_SUMMARIES
    }
  };
}

async function run() {
  const now = Date.now();
  const passwordHash = bcrypt.hashSync(DEFAULT_DEV_PASSWORD, 10);
  const payload = buildSeedData(now, passwordHash);

  if (dryRun) {
    console.log("[DRY RUN] Seed payload prepared:");
    console.log(`- users: ${Object.keys(payload.users).length}`);
    console.log(`- hospitals: ${Object.keys(payload.hospitals).length}`);
    console.log(`- ambulances: ${Object.keys(payload.ambulances).length}`);
    console.log(`- emergencyRequests: ${Object.keys(payload.emergencyRequests).length}`);
    console.log(`- default development password: ${DEFAULT_DEV_PASSWORD}`);
    return;
  }

  const dbUrl = process.env.FIREBASE_DATABASE_URL;
  if (!dbUrl) {
    throw new Error("FIREBASE_DATABASE_URL is required.");
  }

  if (!admin.apps.length) {
    admin.initializeApp({
      credential: admin.credential.applicationDefault(),
      databaseURL: dbUrl
    });
  }

  const db = admin.database();
  await db.ref().update(payload);

  console.log("Firebase seed completed successfully.");
  console.log(`Default development password for all seeded users: ${DEFAULT_DEV_PASSWORD}`);
}

run().catch((err) => {
  console.error("Firebase seed failed:", err.message);
  process.exit(1);
});

