{
  "version": "2.0.0",
  "timestamp": "2026-11-11T05:00:00.000Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "locked": false,
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-wall",
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
      "kind": "atomic",
      "id": "line-mwbij6hj-y7c6ew",
      "endPoint": {
        "x": 14.5,
        "y": 95.7
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 0
      },
      "color": "#59a14f",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "name": "WALL_FLOWER_STAGING park"
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
      "id": "wall-lane",
      "color": "#f28e2b",
      "name": "to LEFT_LAUNCH_CATCH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 123.5
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 110
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 180,
        "endDeg": 273.4,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 180,
                "endDeg": 273.4
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 273.4,
                "endDeg": 273.4
              }
            }
          ]
        }
      }
    },
    {
      "kind": "atomic",
      "id": "line-mwbij6hk-eyn74i",
      "endPoint": {
        "x": 14.5,
        "y": 95.7
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 273.4,
        "endDeg": 0
      },
      "color": "#b07aa1",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "name": "LEFT_LAUNCH_CATCH park"
    },
    {
      "id": "link-2",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 123.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 273.4
      }
    },
    {
      "id": "to-scoring",
      "color": "#b07aa1",
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
        "startDeg": 273.4,
        "endDeg": 90
      }
    },
    {
      "kind": "atomic",
      "id": "line-mwbij6hk-5pezgk",
      "endPoint": {
        "x": 14.5,
        "y": 95.7
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 0
      },
      "color": "#9c755f",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "name": "SCORING_FLOWER_STAGING park"
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
        "degrees": 450
      }
    },
    {
      "id": "scoring-back",
      "color": "#edc948",
      "name": "to LEFT_LAUNCH_OFFSET",
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
    },
    {
      "id": "left-park",
      "color": "#e15759",
      "name": "to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 95.7
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 92
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 296.4,
        "endDeg": 0
      }
    },
    {
      "id": "link-4",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 123.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 273.4
      }
    },
    {
      "id": "to-right-close",
      "color": "#4e79a7",
      "name": "to RIGHT_LAUNCH_CLOSE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 56.1,
        "y": 26
      },
      "controlPoints": [
        {
          "x": 58.77,
          "y": 101.57
        },
        {
          "x": 65.2,
          "y": 71.9
        },
        {
          "x": 59.6,
          "y": 40.9
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 270,
        "endDeg": 90,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.87,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 270,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.87,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "right-park",
      "color": "#9c755f",
      "name": "to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 95.7
      },
      "controlPoints": [
        {
          "x": 23.32,
          "y": 25.55
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
      "lineId": "to-wall"
    },
    {
      "kind": "path",
      "lineId": "line-mwbij6hj-y7c6ew"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "wall-lane"
    },
    {
      "kind": "path",
      "lineId": "line-mwbij6hk-eyn74i"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "to-scoring"
    },
    {
      "kind": "path",
      "lineId": "line-mwbij6hk-5pezgk"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "scoring-back"
    },
    {
      "kind": "path",
      "lineId": "left-park"
    },
    {
      "kind": "path",
      "lineId": "link-4"
    },
    {
      "kind": "path",
      "lineId": "to-right-close"
    },
    {
      "kind": "path",
      "lineId": "right-park"
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
        "PickFlower": 4,
        "GardenGather": 5,
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
      "LEFT_LAUNCH_CATCH": [
        57.5,
        123.5,
        273.4
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "LEFT_LAUNCH_OFFSET": [
        41,
        121.5,
        296.4
      ],
      "RIGHT_PARK": [
        14.5,
        95.7,
        0
      ],
      "RIGHT_LAUNCH_CLOSE": [
        56.1,
        26,
        90
      ]
    },
    "pathEnds": {
      "to-wall": "WALL_FLOWER_STAGING",
      "wall-lane": "LEFT_LAUNCH_CATCH",
      "to-scoring": "SCORING_FLOWER_STAGING",
      "scoring-back": "LEFT_LAUNCH_OFFSET",
      "left-park": "RIGHT_PARK",
      "to-right-close": "RIGHT_LAUNCH_CLOSE",
      "right-park": "RIGHT_PARK",
      "line-mwbij6hj-y7c6ew": "RIGHT_PARK",
      "line-mwbij6hk-eyn74i": "RIGHT_PARK",
      "line-mwbij6hk-5pezgk": "RIGHT_PARK"
    },
    "cards": [
      {
        "id": "c-a-10",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-a-11",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-p-12",
        "kind": "path",
        "lineId": "to-wall",
        "park": false
      },
      {
        "id": "c-a-13",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 4.5
      },
      {
        "id": "c-p-14",
        "kind": "path",
        "lineId": "wall-lane",
        "park": false
      },
      {
        "id": "c-w-15",
        "kind": "firstOf",
        "label": "Wait for TIP or CELL up",
        "rows": [
          {
            "when": [
              "CellUp"
            ],
            "cards": [
              {
                "id": "c-a-1",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-p-2",
                "kind": "path",
                "lineId": "to-scoring",
                "park": false
              },
              {
                "id": "c-a-3",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 4.5
              },
              {
                "id": "c-p-4",
                "kind": "path",
                "lineId": "scoring-back",
                "park": false
              },
              {
                "id": "c-a-5",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-p-6",
                "kind": "path",
                "lineId": "left-park",
                "park": true
              }
            ]
          },
          {
            "afterMs": 750,
            "cards": [
              {
                "id": "c-p-7",
                "kind": "path",
                "lineId": "to-right-close",
                "park": false
              },
              {
                "id": "c-a-8",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-p-9",
                "kind": "path",
                "lineId": "right-park",
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
      "link-2",
      "link-3",
      "link-4"
    ],
    "parkFrom": {
      "c-p-12": "line-mwbij6hj-y7c6ew",
      "c-p-14": "line-mwbij6hk-eyn74i",
      "c-p-2": "line-mwbij6hk-5pezgk"
    },
    "teamSpots": [
      "LEFT_LAUNCH_CATCH",
      "LEFT_LAUNCH_OFFSET",
      "RIGHT_LAUNCH_CLOSE",
      "RIGHT_PARK",
      "RIGHT_START",
      "SCORING_FLOWER_STAGING",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "R-Two-Tip-With"
  }
}
