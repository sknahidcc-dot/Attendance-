# Job Express Attendance — সম্পূর্ণ সেটআপ ও পরিচালনা নির্দেশিকা

এই নির্দেশিকাটি অনুসরণ করে আপনি আপনার মোবাইল থেকেই কোনো কম্পিউটার বা সাবস্ক্রিপশন ফি ছাড়াই সম্পূর্ণ বিনামূল্যে **Job Express Attendance** অ্যাপটি Google Sheets-এর সাথে যুক্ত করে পরিচালনা করতে পারবেন।

---

## ১. Google Spreadsheet ও Apps Script সেটআপ (১০০% ফ্রি)

### ধাপ ১: নতুন Google Spreadsheet তৈরি
১. আপনার মোবাইলে বা ব্রাউজারে [sheets.google.com](https://sheets.google.com) এ যান।
২. একটি নতুন স্প্রেডশিট খুলুন এবং এর নাম দিন **"Job Express Attendance Database"**।
৩. নিচে দুটি পৃথক ট্যাব (Sheet) তৈরি করুন:
   - **ট্যাব ১ এর নাম:** `Library`
   - **ট্যাব ২ এর নাম:** `Coaching`

### ধাপ ২: কলামের নাম লিখুন
- **Library ট্যাবের প্রথম সারিতে লিখুন:**
  `শিক্ষার্থীর নাম` | `LID` | `সদস্যপদের অবস্থা` | `তারিখ` | `উপস্থিতির সময়` | `উপস্থিতির অবস্থা`
- **Coaching ট্যাবের প্রথম সারিতে লিখুন:**
  `শিক্ষার্থীর নাম` | `CID` | `ব্যাচ` | `সদস্যপদের অবস্থা` | `তারিখ` | `উপস্থিতির সময়` | `উপস্থিতির অবস্থা`

### ধাপ ৩: Google Apps Script ব্যাকএন্ড যুক্ত করা
১. স্প্রেডশিটের ওপরের মেন্যু থেকে **Extensions > Apps Script**-এ যান।
২. স্ক্রিপ্ট এডিটরে বিদ্যমান ডিফল্ট কোড মুছে দিয়ে নিচের সম্পূর্ণ কোডটি পেস্ট করুন:

```javascript
// ==========================================
// Job Express Attendance - Google Apps Script
// ==========================================

function doPost(e) {
  var lock = LockService.getScriptLock();
  lock.tryLock(10000);
  
  try {
    var rawData = e.postData.contents;
    var data = JSON.parse(rawData);
    var action = data.action;
    var ss = SpreadsheetApp.getActiveSpreadsheet();

    if (action === "markAttendance") {
      var dept = data.department; // "LIBRARY" or "COACHING"
      var sheetName = (dept === "LIBRARY") ? "Library" : "Coaching";
      var sheet = ss.getSheetByName(sheetName);
      if (!sheet) {
        sheet = ss.insertSheet(sheetName);
        if (dept === "LIBRARY") {
          sheet.appendRow(["শিক্ষার্থীর নাম", "LID", "সদস্যপদের অবস্থা", "তারিখ", "উপস্থিতির সময়", "উপস্থিতির অবস্থা"]);
        } else {
          sheet.appendRow(["শিক্ষার্থীর নাম", "CID", "ব্যাচ", "সদস্যপদের অবস্থা", "তারিখ", "উপস্থিতির সময়", "উপস্থিতির অবস্থা"]);
        }
      }

      var studentId = (dept === "LIBRARY") ? data.lid : data.cid;
      var today = data.date;
      var values = sheet.getDataRange().getValues();
      
      // ডুপ্লিকেট উপস্থিতি প্রতিরোধ (একই শিক্ষার্থী, একই দিনে, একই বিভাগ)
      for (var i = 1; i < values.length; i++) {
        var rowId = values[i][1];
        var rowDate = values[i][3];
        if (rowId == studentId && rowDate == today) {
          return ContentService.createTextOutput(JSON.stringify({
            success: false,
            message: "আপনি আজকের দিনে ইতিমধ্যে উপস্থিতি দিয়েছেন!"
          })).setMimeType(ContentService.MimeType.JSON);
        }
      }

      // সার্ভার টাইম ও ডেট সংরক্ষণ (ঢাকা টাইমজোন)
      var now = new Date();
      var serverTime = Utilities.formatDate(now, "Asia/Dhaka", "hh:mm:ss a");
      var serverDate = Utilities.formatDate(now, "Asia/Dhaka", "yyyy-MM-dd");

      if (dept === "LIBRARY") {
        sheet.appendRow([data.studentName, data.lid, "Active", serverDate, serverTime, "উপস্থিত"]);
      } else {
        sheet.appendRow([data.studentName, data.cid, data.batch, "Active", serverDate, serverTime, "উপস্থিত"]);
      }

      return ContentService.createTextOutput(JSON.stringify({
        success: true,
        message: "উপস্থিতি সফলভাবে গ্রহণ করা হয়েছে!",
        serverDate: serverDate,
        serverTime: serverTime
      })).setMimeType(ContentService.MimeType.JSON);
    }

    return ContentService.createTextOutput(JSON.stringify({
      success: false,
      message: "অজানা অ্যাকশন"
    })).setMimeType(ContentService.MimeType.JSON);

  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({
      success: false,
      message: err.toString()
    })).setMimeType(ContentService.MimeType.JSON);
  } finally {
    lock.releaseLock();
  }
}

function doGet(e) {
  var action = e.parameter.action;
  var ss = SpreadsheetApp.getActiveSpreadsheet();

  if (action === "getAllData") {
    var libSheet = ss.getSheetByName("Library");
    var coachSheet = ss.getSheetByName("Coaching");
    var records = [];

    if (libSheet) {
      var libData = libSheet.getDataRange().getValues();
      for (var i = 1; i < libData.length; i++) {
        records.push({
          studentName: libData[i][0],
          lid: libData[i][1],
          date: libData[i][3],
          time: libData[i][4],
          status: libData[i][5],
          department: "LIBRARY"
        });
      }
    }

    if (coachSheet) {
      var cData = coachSheet.getDataRange().getValues();
      for (var j = 1; j < cData.length; j++) {
        records.push({
          studentName: cData[j][0],
          cid: cData[j][1],
          batch: cData[j][2],
          date: cData[j][4],
          time: cData[j][5],
          status: cData[j][6],
          department: "COACHING"
        });
      }
    }

    return ContentService.createTextOutput(JSON.stringify({
      success: true,
      records: records
    })).setMimeType(ContentService.MimeType.JSON);
  }

  return ContentService.createTextOutput(JSON.stringify({
    success: true,
    message: "Job Express Attendance API Active"
  })).setMimeType(ContentService.MimeType.JSON);
}
```

### ধাপ ৪: Web App হিসেবে Deploy করা
১. উপরে ডানপাশে **Deploy > New deployment** বাটনে ক্লিক করুন।
২. Gear আইকন থেকে **Web app** সিলেক্ট করুন।
৩. অপশনগুলো এভাবে পূরণ করুন:
   - **Description:** `Job Express API v1`
   - **Execute as:** `Me` (আপনার গুগল অ্যাকাউন্ট)
   - **Who has access:** `Anyone` (যাতে অ্যাপ থেকে সরাসরি রিকোয়েস্ট আসতে পারে)
৪. **Deploy** বাটনে ক্লিক করে Google Permission এলাউ করুন।
৫. শেষ ধাপে একটি **Web app URL** পাবেন (যেমন: `https://script.google.com/macros/s/AKfycbx.../exec`)। এই লিংকটি কপি করুন।

---

## ২. অ্যাপে URL যুক্ত করা ও অ্যাডমিন ড্যাশবোর্ড

১. অ্যাপের হোম পেজের উপরে ডান কোণে থাকা ছোট **Admin বাটন**টিতে ক্লিক করুন।
২. অ্যাডমিন ক্রেডেনশিয়াল দিয়ে লগইন করুন:
   - **Username:** `Job`
   - **Password:** `express`
৩. অ্যাডমিন ড্যাশবোর্ডের **"সেটিংস"** ট্যাবে যান।
৪. আপনার কপি করা Apps Script Web App URL টি পেস্ট করে **"সংরক্ষণ করুন"** বাটনে চাপ দিন।
৫. ব্যস! এখন থেকে সব উপস্থিতির রেকর্ড সরাসরি আপনার Google Sheets-এ স্বয়ংক্রিয়ভাবে লাইভ সেভ হবে।

---

## ৩. দেয়ালে QR কোড স্থাপন ও প্রিন্ট করার নিয়ম

১. অ্যাডমিন ড্যাশবোর্ডের **"দেয়াল QR তৈরি"** ট্যাবে যান।
২. **লাইব্রেরি QR** এবং **কোচিং QR** পৃথকভাবে দেখতে পাবেন।
৩. **"শেয়ার / প্রিন্ট করুন"** বাটনে চাপ দিয়ে ছবিটি আপনার মোবাইলে সেভ করে নিন অথবা সরাসরি প্রিন্ট করুন।
৪. লাইব্রেরির প্রবেশ দ্বারে লাইব্রেরি QR এবং কোচিং ক্লাসরুমের দেয়ালে কোচিং QR স্থাপন করুন।
৫. কেউ QR নকল করার চেষ্টা করলে ড্যাশবোর্ড থেকে **"নতুন সিকিউর QR"** বাটনে চাপ দিয়ে যেকোনো সময় নতুন টোকেন তৈরি করতে পারবেন।

---

## ৪. কম্পিউটার ছাড়া মোবাইল দিয়ে GitHub Actions থেকে APK ডাউনলোড

এই প্রজেক্টে অটোমেটিক **GitHub Actions Workflow** যুক্ত করা হয়েছে।
১. এই প্রজেক্টটি GitHub-এ পুশ/আপলোড করলেই GitHub Actions ব্যাকগ্রাউন্ডে স্বয়ংক্রিয়ভাবে APK বিল্ড করা শুরু করবে।
২. আপনার মোবাইল থেকে GitHub রিপোজিটরির **"Actions"** ট্যাবে যান।
৩. সেখানে সফল হওয়া বিল্ডের ওপর ক্লিক করলে নিচে **"Artifacts"** সেকশনে `Job-Express-Attendance-APK` দেখতে পাবেন।
৪. এছাড়া **Releases** সেকশনে গিয়েও সরাসরি এক ক্লিকে অফিশিয়াল রিলিজ APK ডাউনলোড করে মোবাইলে ইন্সটল করতে পারবেন। কোনো কম্পিউটার লাগবে না!
