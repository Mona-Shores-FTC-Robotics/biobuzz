{
  "version": "2.0.0",
  "timestamp": "2026-11-17T17:25:24.227Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "locked": false,
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "t-wall-flower",
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
      "id": "t-wall-park",
      "color": "#f28e2b",
      "name": "WALL_FLOWER_STAGING park",
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
      "controlPoints": [],
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
      "id": "t-lane-north",
      "color": "#59a14f",
      "name": "wall lane north to LEFT_TURRET_FIRE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 38,
        "y": 126
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 60
        },
        {
          "x": 33,
          "y": 92
        },
        {
          "x": 35,
          "y": 104
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
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "t-fire-park",
      "color": "#59a14f",
      "name": "LEFT_TURRET_FIRE park",
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
          "x": 30,
          "y": 112
        },
        {
          "x": 24,
          "y": 99
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 0
      }
    },
    {
      "id": "link-2",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 38,
        "y": 126
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "t-hop-flower",
      "color": "#59a14f",
      "name": "hop to SCORING_FLOWER_STAGING",
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
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "t-flower-park",
      "color": "#f28e2b",
      "name": "SCORING_FLOWER_STAGING park",
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
          "x": 40,
          "y": 96
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 0
      }
    },
    {
      "id": "link-3",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 47.2,
        "y": 119.3
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "t-hop-fire",
      "color": "#59a14f",
      "name": "hop back to LEFT_TURRET_FIRE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 38,
        "y": 126
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
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
      "lineId": "t-wall-flower"
    },
    {
      "kind": "path",
      "lineId": "t-wall-park"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "t-lane-north"
    },
    {
      "kind": "path",
      "lineId": "t-fire-park"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "t-hop-flower"
    },
    {
      "kind": "path",
      "lineId": "t-flower-park"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "t-hop-fire"
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
        "CellUp",
        "Always"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 2.5,
        "PickFlower": 4,
        "GardenGather": 4,
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
      "LEFT_TURRET_FIRE": [
        38,
        126,
        90
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "RIGHT_PARK": [
        14.5,
        98.7,
        0
      ]
    },
    "pathEnds": {
      "t-wall-flower": "WALL_FLOWER_STAGING",
      "t-wall-park": "RIGHT_PARK",
      "t-lane-north": "LEFT_TURRET_FIRE",
      "t-fire-park": "RIGHT_PARK",
      "t-hop-flower": "SCORING_FLOWER_STAGING",
      "t-flower-park": "RIGHT_PARK",
      "t-hop-fire": "LEFT_TURRET_FIRE"
    },
    "cards": [
      {
        "id": "T-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "T-launch-preloads",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "T-wall-flower",
        "kind": "path",
        "lineId": "t-wall-flower",
        "park": false
      },
      {
        "id": "T-pick-wall",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 4.5
      },
      {
        "id": "T-lane-north",
        "kind": "path",
        "lineId": "t-lane-north",
        "park": false
      },
      {
        "id": "T-launch-wall-load",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "T-hop-flower",
        "kind": "path",
        "lineId": "t-hop-flower",
        "park": false
      },
      {
        "id": "T-pick-scoring",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 4.5
      },
      {
        "id": "T-hop-fire",
        "kind": "path",
        "lineId": "t-hop-fire",
        "park": false
      },
      {
        "id": "T-launch-scoring-load",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "T-park",
        "kind": "path",
        "lineId": "t-fire-park",
        "park": true
      }
    ],
    "startAt": "RIGHT_START",
    "linkPaths": [
      "link-1",
      "link-2",
      "link-3"
    ],
    "parkFrom": {
      "T-wall-flower": "t-wall-park",
      "T-lane-north": "t-fire-park",
      "T-hop-flower": "t-flower-park",
      "T-hop-fire": "t-fire-park"
    },
    "teamSpots": [
      "RIGHT_PARK",
      "RIGHT_START",
      "SCORING_FLOWER_STAGING",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "R-Solo-Turret",
    "turretAims": true
  }
}