{
  "version": "2.0.0",
  "timestamp": "2026-11-04T12:00:00.000Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "locked": false,
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "line-r3b-3",
      "color": "#59a14f",
      "name": "to WALL_FLOWER_STAGING",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 180
      }
    },
    {
      "id": "line-r3b-5",
      "color": "#59a14f",
      "name": "wall lane to LEFT_LAUNCH_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 121.5
      },
      "controlPoints": [
        {
          "x": 33.0,
          "y": 120.0
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 296.4
      }
    },
    {
      "id": "line-r3b-7",
      "color": "#59a14f",
      "name": "to SCORING_FLOWER_STAGING",
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
        "startDeg": 296.4,
        "endDeg": 90
      }
    },
    {
      "id": "line-r3b-9",
      "color": "#59a14f",
      "name": "wall lane to RIGHT_LAUNCH_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 20
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 100
        },
        {
          "x": 28,
          "y": 45
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 68.7
      }
    },
    {
      "id": "line-r3b-11",
      "color": "#59a14f",
      "name": "to SCORING_FLOWER_STAGING ",
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
        "startDeg": 296.4,
        "endDeg": 90
      }
    },
    {
      "id": "line-r3b-13",
      "color": "#59a14f",
      "name": "back to LEFT_LAUNCH_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 121.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 296.4
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
      "lineId": "line-r3b-3"
    },
    {
      "kind": "path",
      "lineId": "line-r3b-5"
    },
    {
      "kind": "path",
      "lineId": "line-r3b-7"
    },
    {
      "kind": "path",
      "lineId": "line-r3b-9"
    },
    {
      "kind": "path",
      "lineId": "line-r3b-11"
    },
    {
      "kind": "path",
      "lineId": "line-r3b-13"
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
    "maxAcceleration": 50,
    "maxDeceleration": 50,
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
        "CameraBlind",
        "CellUp"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 4,
        "PickFlower": 4.4,
        "GardenGather": 6,
        "LaunchOne": 0.5,
        "SpinUp": 0.1
      }
    },
    "points": {
      "RIGHT_START": [
        55.6,
        7.7,
        90
      ],
      "WALL_FLOWER_STAGING": [
        23.1,
        47.2,
        180
      ],
      "LEFT_LAUNCH_OFFSET": [
        41,
        121.5,
        296.4
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "RIGHT_LAUNCH_OFFSET": [
        41,
        20,
        68.7
      ]
    },
    "pathEnds": {
      "line-r3b-3": "WALL_FLOWER_STAGING",
      "line-r3b-5": "LEFT_LAUNCH_OFFSET",
      "line-r3b-7": "SCORING_FLOWER_STAGING",
      "line-r3b-9": "RIGHT_LAUNCH_OFFSET",
      "line-r3b-11": "SCORING_FLOWER_STAGING",
      "line-r3b-13": "LEFT_LAUNCH_OFFSET"
    },
    "cards": [
      {
        "id": "c-r3b-1",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-r3b-2",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-r3b-3",
        "kind": "path",
        "lineId": "line-r3b-3",
        "park": false
      },
      {
        "id": "c-r3b-4",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 8
      },
      {
        "id": "c-r3b-5",
        "kind": "path",
        "lineId": "line-r3b-5",
        "park": false
      },
      {
        "id": "c-r3b-6",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-r3b-15",
        "kind": "firstOf",
        "label": "Did TIP 2 come?",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": [
              {
                "id": "c-r3b-7",
                "kind": "path",
                "lineId": "line-r3b-7",
                "park": false
              },
              {
                "id": "c-r3b-8",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 8
              },
              {
                "id": "c-r3b-9",
                "kind": "path",
                "lineId": "line-r3b-9",
                "park": false
              },
              {
                "id": "c-r3b-10",
                "kind": "action",
                "name": "LaunchAll"
              }
            ]
          },
          {
            "afterMs": 2500,
            "cards": [
              {
                "id": "c-r3b-11",
                "kind": "path",
                "lineId": "line-r3b-11",
                "park": false
              },
              {
                "id": "c-r3b-12",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 8
              },
              {
                "id": "c-r3b-13",
                "kind": "path",
                "lineId": "line-r3b-13",
                "park": false
              },
              {
                "id": "c-r3b-14",
                "kind": "action",
                "name": "LaunchAll"
              }
            ]
          }
        ]
      }
    ],
    "startAt": "RIGHT_START",
    "teamSpots": [
      "LEFT_LAUNCH_OFFSET",
      "RIGHT_LAUNCH_OFFSET",
      "RIGHT_START",
      "SCORING_FLOWER_STAGING",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "R-3Tip-B"
  }
}