{
  "version": "2.0.0",
  "timestamp": "2026-10-31T16:44:07.900Z",
  "startPoint": {
    "x": 62.3,
    "y": 133.8,
    "locked": false,
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "line-mvwmh60z-albd6s",
      "color": "#9c755f",
      "name": "to LAUNCH_LEFT",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.4,
        "y": 129.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "line-mvwmgt7p-3vf6y5",
      "color": "#f28e2b",
      "name": "to SCORING_FLOWER",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.2,
        "y": 119.3
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 90
      }
    },
    {
      "id": "line-mvwmh0b8-hjreg5",
      "color": "#b07aa1",
      "name": "to LAUNCH_LEFT",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.4,
        "y": 129.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "line-mvwmhsd0-9jt1zg",
      "color": "#59a14f",
      "name": "to PARK_LEFT",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.1,
        "y": 117.4
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 0
      }
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
  "sequence": [
    {
      "kind": "path",
      "lineId": "line-mvwmh60z-albd6s"
    },
    {
      "kind": "path",
      "lineId": "line-mvwmgt7p-3vf6y5"
    },
    {
      "kind": "path",
      "lineId": "line-mvwmh0b8-hjreg5"
    },
    {
      "kind": "path",
      "lineId": "line-mvwmhsd0-9jt1zg"
    }
  ],
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
    "maxVelocity": 50,
    "maxAcceleration": 45,
    "maxDeceleration": 45,
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
      "actions": [
        "LaunchAll",
        "PickFlower",
        "GardenGather",
        "LaunchOne",
        "SpinUp"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "LeftCellUp",
        "RightCellUp",
        "Tip",
        "CameraBlind"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 2,
        "PickFlower": 4.5,
        "GardenGather": 6,
        "LaunchOne": 0.5,
        "SpinUp": 0.1
      }
    },
    "points": {
      "START_LEFT": [
        62.3,
        133.8,
        270
      ],
      "SCORING_FLOWER": [
        47.2,
        119.3,
        90
      ],
      "LAUNCH_LEFT": [
        59.4,
        129.5,
        270
      ],
      "PARK_LEFT": [
        14.1,
        117.4,
        0
      ]
    },
    "pathEnds": {
      "line-mvwmgt7p-3vf6y5": "SCORING_FLOWER",
      "line-mvwmh0b8-hjreg5": "LAUNCH_LEFT",
      "line-mvwmh60z-albd6s": "LAUNCH_LEFT",
      "line-mvwmhsd0-9jt1zg": "PARK_LEFT"
    },
    "cards": [
      {
        "id": "c-mvwmb061-m6kmy",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-mvwmh60z-gmdlm",
        "kind": "path",
        "lineId": "line-mvwmh60z-albd6s",
        "park": false
      },
      {
        "id": "c-mvwmgja9-wj4uv",
        "kind": "firstOf",
        "label": "Wait for TIP",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 7000,
            "cards": []
          }
        ]
      },
      {
        "id": "c-mvwmgonl-zfwee",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-mvwmgt7p-6e2sq",
        "kind": "path",
        "lineId": "line-mvwmgt7p-3vf6y5",
        "park": false
      },
      {
        "id": "c-mvwmgum1-iwq33",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 8
      },
      {
        "id": "c-mvwmh0b8-xsmnr",
        "kind": "path",
        "lineId": "line-mvwmh0b8-hjreg5",
        "park": false
      },
      {
        "id": "c-mvwmhnro-39vkc",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-mvwmhsd0-72210",
        "kind": "path",
        "lineId": "line-mvwmhsd0-9jt1zg",
        "park": true
      }
    ],
    "startAt": "START_LEFT",
    "teamSpots": [
      "LAUNCH_LEFT",
      "PARK_LEFT",
      "SCORING_FLOWER",
      "START_LEFT"
    ]
  }
}