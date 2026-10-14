package com.example.nomergrammuz.domain.model

enum class PlateCategory {
    DIPLOMATIC,
    STATE_AUTHORITY,
    LAW_ENFORCEMENT,
    MILITARY,
    EMERGENCY,
    REGIONAL,
    CIVIL_INDIVIDUAL,
    CIVIL_LEGAL,
    TAXI,
    UNKNOWN
}

enum class PlateTheme {
    WHITE,
    GREEN,              // Diplomatic (CMD, D, T, X)
    BLUE,               // UN agencies
    YELLOW,             // Foreign citizens (H), Taxi
    WHITE_GOV_FLAG,     // Type II (PAA, PPP, APC, etc. with national flag on the left, no region)
    EV_GREEN_ZONE,      // White plate with green region block (Electric Vehicles)
    BLACK               // Military vehicles (Mudofaa vazirligi - black background, white text)
}

enum class PlateFormatType {
    DIPLOMATIC_HEAD,    // CMD
    DIPLOMAT,           // D
    UN_AGENCY,          // UN
    TECHNICAL_STAFF,    // T
    CONSULAR,           // X
    FOREIGN_FIRM,       // M
    FOREIGN_CITIZEN,    // H
    GOV_SPECIAL,        // PAA, PSS, PPP, APC, UZB, ZDF, etc. (Type II)
    GOV_DUTY_REGIONAL,  // DAV (e.g. 01 123 DAV)
    CIVILIAN_INDIVIDUAL,// 01 A 123 AA (Type III)
    CIVILIAN_LEGAL,     // 01 123 AAA (Type I)
    PUBLIC_TAXI,        // 01 123 TAA..TTG
    EMERGENCY_SERVICE,  // 01 123 STY
    MILITARY,           // 01 1234 MV (Black plate, white text)
    UNKNOWN
}
