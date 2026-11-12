{
  "version": "2.0.0",
  "timestamp": "2026-11-11T00:00:00.000Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "headingDeg": 90,
    "locked": false
  },
  "lines": [
    {
      "kind": "atomic",
      "id": "r-wall-flower",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 180
      },
      "locked": false,
      "name": "wall flower to WALL_FLOWER_STAGING",
      "color": "#e15759",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "r-wall-lane-north",
      "endPoint": {
        "x": 41,
        "y": 121.5
      },
      "controlPoints": [
        {
          "x": 24,
          "y": 85
        },
        {
          "x": 36,
          "y": 108
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 296.4
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 296.4
              }
            }
          ]
        }
      },
      "locked": false,
      "name": "wall lane north to LEFT_LAUNCH_OFFSET",
      "color": "#e15759",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "r-left-park",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 41,
          "y": 100
        },
        {
          "x": 30,
          "y": 95.7
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 296.4,
        "endDeg": 360
      },
      "locked": false,
      "name": "left park to RIGHT_PARK",
      "color": "#e15759",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "r-link-wall",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 180
      },
      "locked": false,
      "name": "Link to WALL_FLOWER_STAGING"
    },
    {
      "kind": "atomic",
      "id": "r-wall-abort-park",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 75
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 0
      },
      "locked": false,
      "name": "wall abort park to RIGHT_PARK",
      "color": "#e15759",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
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
        "LaunchOne",
        "SpinUp",
        "PickFlower",
        "GardenGather"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "LeftCellUp",
        "RightCellUp",
        "CellUp",
        "Tip",
        "CameraBlind"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 4.5,
        "LaunchOne": 0.5,
        "SpinUp": 0.1,
        "PickFlower": 4.0,
        "GardenGather": 5.0
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
      "RIGHT_PARK": [
        14.5,
        98.7,
        0
      ]
    },
    "pathEnds": {
      "r-wall-flower": "WALL_FLOWER_STAGING",
      "r-wall-lane-north": "LEFT_LAUNCH_OFFSET",
      "r-left-park": "RIGHT_PARK",
      "r-wall-abort-park": "RIGHT_PARK"
    },
    "cards": [
      {
        "id": "R-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "R-launch-preloads",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "card-r-wall-flower",
        "kind": "path",
        "lineId": "r-wall-flower",
        "park": false
      },
      {
        "id": "R-pick-wall",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 4.5
      },
      {
        "id": "card-r-wall-lane-north",
        "kind": "path",
        "lineId": "r-wall-lane-north",
        "park": false
      },
      {
        "id": "R-wait-left",
        "kind": "firstOf",
        "label": "LEFT up before 14 s, otherwise park",
        "rows": [
          {
            "afterMs": 14000,
            "matchClock": true,
            "cards": [
              {
                "id": "card-r-left-park-timeout",
                "kind": "path",
                "lineId": "r-left-park",
                "park": true
              }
            ]
          },
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [
              {
                "id": "R-launch-left",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "card-r-left-park",
                "kind": "path",
                "lineId": "r-left-park",
                "park": true
              }
            ]
          }
        ]
      }
    ],
    "startAt": "RIGHT_START",
    "exportName": "R-OutsideLane-Short",
    "parkFrom": {
      "card-r-wall-flower": "r-wall-abort-park",
      "card-r-wall-lane-north": "r-left-park"
    }
  }
}
