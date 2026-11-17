{
  "version": "2.0.0",
  "timestamp": "2026-11-17T16:43:04.077Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "locked": false,
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "r-wall-flower",
      "color": "#e15759",
      "name": "wall flower to WALL_FLOWER_STAGING",
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
      "id": "r-wall-abort-park",
      "color": "#e15759",
      "name": "wall abort park to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
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
      }
    },
    {
      "id": "link-1",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "r-wall-lane-north",
      "color": "#e15759",
      "name": "wall lane north to LEFT_AUTO_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 60
        },
        {
          "x": 34,
          "y": 92
        },
        {
          "x": 35,
          "y": 100
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.62,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.62,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 360,
                "endDeg": 298.7
              }
            }
          ]
        }
      }
    },
    {
      "id": "r-left-park",
      "color": "#e15759",
      "name": "left park to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 44,
          "y": 104
        },
        {
          "x": 34,
          "y": 96
        },
        {
          "x": 26,
          "y": 98.7
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 298.7,
                "endDeg": 360
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 360
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-2",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 298.70000000000005
      }
    },
    {
      "id": "r-scoring-flower",
      "color": "#e15759",
      "name": "scoring flower to SCORING_FLOWER_STAGING",
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
        "startDeg": 298.7,
        "endDeg": 450
      }
    },
    {
      "id": "r-flower-abort-park",
      "color": "#e15759",
      "name": "flower abort park to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 38,
          "y": 112.6
        },
        {
          "x": 31.8,
          "y": 94.1
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
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
      "lineId": "r-wall-flower"
    },
    {
      "kind": "path",
      "lineId": "r-wall-abort-park"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "r-wall-lane-north"
    },
    {
      "kind": "path",
      "lineId": "r-left-park"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "r-scoring-flower"
    },
    {
      "kind": "path",
      "lineId": "r-flower-abort-park"
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
        "LaunchOne",
        "SpinUp",
        "PickFlower",
        "PickFlowerLaunch",
        "GardenGather"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "LeftCellUp",
        "RightCellUp",
        "CellUp",
        "Tip",
        "CameraBlind",
        "Always"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 2.5,
        "LaunchOne": 0.5,
        "SpinUp": 0.1,
        "PickFlower": 4,
        "PickFlowerLaunch": 6,
        "GardenGather": 4
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
      "LEFT_AUTO_OFFSET": [
        40.7,
        121.1,
        298.7
      ],
      "RIGHT_PARK": [
        14.5,
        98.7,
        0
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ]
    },
    "pathEnds": {
      "r-wall-flower": "WALL_FLOWER_STAGING",
      "r-wall-lane-north": "LEFT_AUTO_OFFSET",
      "r-left-park": "RIGHT_PARK",
      "r-wall-abort-park": "RIGHT_PARK",
      "r-scoring-flower": "SCORING_FLOWER_STAGING",
      "r-flower-abort-park": "RIGHT_PARK"
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
        "id": "R-check-left",
        "kind": "firstOf",
        "label": "LEFT CELL up?",
        "rows": [
          {
            "when": [
              "CellUp"
            ],
            "cards": [
              {
                "id": "card-r-scoring-flower",
                "kind": "path",
                "lineId": "r-scoring-flower",
                "park": false
              },
              {
                "id": "R-pick-launch",
                "kind": "action",
                "name": "PickFlowerLaunch",
                "timeoutS": 8
              },
              {
                "id": "card-r-flower-park",
                "kind": "path",
                "lineId": "r-flower-abort-park",
                "park": true
              }
            ]
          },
          {
            "afterMs": 750,
            "cards": [
              {
                "id": "card-r-left-park-no-tip",
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
    "linkPaths": [
      "link-1",
      "link-2"
    ],
    "parkFrom": {
      "card-r-wall-flower": "r-wall-abort-park",
      "card-r-wall-lane-north": "r-left-park",
      "card-r-scoring-flower": "r-flower-abort-park"
    },
    "teamSpots": [
      "LEFT_AUTO_OFFSET",
      "RIGHT_PARK",
      "RIGHT_START",
      "SCORING_FLOWER_STAGING",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "R-Flower-Volley"
  }
}