{
  "version": "2.0.0",
  "timestamp": "2026-11-06T07:54:05.879Z",
  "startPoint": {
    "x": 11.7538,
    "y": 64.193,
    "locked": false,
    "headingDeg": 250
  },
  "lines": [
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "nudge-test-push",
      "name": "Push 6 inches",
      "endPoint": {
        "x": 13.806,
        "y": 69.8312
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 250
      },
      "color": "#f28e2b"
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "nudge-test-release",
      "name": "Back away from partner",
      "endPoint": {
        "x": 17.806,
        "y": 63.8312
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 250
      },
      "color": "#f28e2b"
    }
  ],
  "shapes": [
    {
      "id": "biobuzz-red-hive",
      "name": "Red HIVE",
      "vertices": [
        {
          "x": 46,
          "y": 50
        },
        {
          "x": 48.6,
          "y": 50
        },
        {
          "x": 48.6,
          "y": 91.4
        },
        {
          "x": 46,
          "y": 91.4
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "biobuzz-blue-hive",
      "name": "Blue HIVE",
      "vertices": [
        {
          "x": 96.5,
          "y": 91.4
        },
        {
          "x": 93.9,
          "y": 91.4
        },
        {
          "x": 93.9,
          "y": 50
        },
        {
          "x": 96.5,
          "y": 50
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    },
    {
      "id": "biobuzz-flower-top",
      "name": "Scoring-Side FLOWER",
      "vertices": [
        {
          "x": 43.4,
          "y": 141.5
        },
        {
          "x": 50.1,
          "y": 141.5
        },
        {
          "x": 50.1,
          "y": 139.4
        },
        {
          "x": 46.75,
          "y": 137.7
        },
        {
          "x": 43.4,
          "y": 139.4
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "biobuzz-flower-right",
      "name": "Blue Alliance FLOWER",
      "vertices": [
        {
          "x": 141.5,
          "y": 98.1
        },
        {
          "x": 139.4,
          "y": 98.1
        },
        {
          "x": 137.7,
          "y": 94.75
        },
        {
          "x": 139.4,
          "y": 91.4
        },
        {
          "x": 141.5,
          "y": 91.4
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    },
    {
      "id": "biobuzz-flower-bottom",
      "name": "Audience-Side FLOWER",
      "vertices": [
        {
          "x": 91.15,
          "y": 0
        },
        {
          "x": 97.8,
          "y": 0
        },
        {
          "x": 97.8,
          "y": 2.1
        },
        {
          "x": 94.5,
          "y": 3.8
        },
        {
          "x": 91.15,
          "y": 2.1
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    },
    {
      "id": "biobuzz-flower-left",
      "name": "Red Alliance FLOWER",
      "vertices": [
        {
          "x": 0,
          "y": 50.35
        },
        {
          "x": 2.1,
          "y": 50.35
        },
        {
          "x": 3.8,
          "y": 47
        },
        {
          "x": 2.1,
          "y": 43.65
        },
        {
          "x": 0,
          "y": 43.65
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    }
  ],
  "sequence": [],
  "fieldPoints": [],
  "robot": {
    "forwardVelocity": 75,
    "strafeVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rLength": 15.44,
    "rWidth": 16,
    "rForward": 0,
    "safetyMargin": 1,
    "maxVelocity": 4,
    "maxAcceleration": 8,
    "maxDeceleration": 8,
    "features": [
      {
        "name": "FLOWER poker",
        "length": 1.5,
        "width": 1.5,
        "forward": 8.5,
        "left": 0,
        "collides": true
      }
    ]
  },
  "activePaths": [],
  "auto": {
    "version": 1,
    "drawnFor": "RED",
    "registry": {
      "actions": [],
      "conditions": [],
      "events": [],
      "typicalS": {}
    },
    "points": {
      "PARTNER_CONTACT": [
        11.7538,
        64.193,
        250
      ],
      "PARTNER_PUSH_END": [
        13.806,
        69.8312,
        250
      ],
      "PARTNER_RELEASE": [
        17.806,
        63.8312,
        250
      ]
    },
    "pathEnds": {
      "nudge-test-push": "PARTNER_PUSH_END",
      "nudge-test-release": "PARTNER_RELEASE"
    },
    "cards": [
      {
        "id": "test-push",
        "kind": "path",
        "lineId": "nudge-test-push",
        "park": false
      },
      {
        "id": "test-release",
        "kind": "path",
        "lineId": "nudge-test-release",
        "park": false
      }
    ],
    "startAt": "PARTNER_CONTACT",
    "exportName": "Partner-Nudge-Test"
  }
}
