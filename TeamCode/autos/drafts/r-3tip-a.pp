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
      "id": "line-r3a-3",
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
      "id": "line-r3a-5",
      "color": "#59a14f",
      "name": "centre lane to LEFT_LAUNCH_CATCH",
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
          "x": 63.8,
          "y": 36.1
        },
        {
          "x": 61.3,
          "y": 40.2
        },
        {
          "x": 62.5,
          "y": 45.6
        },
        {
          "x": 62.9,
          "y": 83.6
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.19,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 180,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.19,
              "endProgress": 0.88,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.88,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 270,
                "endDeg": 273.4
              }
            }
          ]
        }
      }
    },
    {
      "id": "line-r3a-8",
      "color": "#59a14f",
      "name": "centre lane to RIGHT_LAUNCH_CATCH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 18
      },
      "controlPoints": [
        {
          "x": 59.5,
          "y": 43.6
        },
        {
          "x": 57.5,
          "y": 41.8
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.42,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 273.4,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.42,
              "endProgress": 0.86,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.86,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 270,
                "endDeg": 91.9
              }
            }
          ]
        }
      }
    },
    {
      "id": "line-r3a-10",
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
        "startDeg": 273.4,
        "endDeg": 90
      }
    },
    {
      "id": "line-r3a-12",
      "color": "#59a14f",
      "name": "back to LEFT_LAUNCH_CATCH",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 273.4
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
      "lineId": "line-r3a-3"
    },
    {
      "kind": "path",
      "lineId": "line-r3a-5"
    },
    {
      "kind": "path",
      "lineId": "line-r3a-8"
    },
    {
      "kind": "path",
      "lineId": "line-r3a-10"
    },
    {
      "kind": "path",
      "lineId": "line-r3a-12"
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
      "LEFT_LAUNCH_CATCH": [
        57.5,
        123.5,
        273.4
      ],
      "RIGHT_LAUNCH_CATCH": [
        57.5,
        18,
        91.9
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ]
    },
    "pathEnds": {
      "line-r3a-3": "WALL_FLOWER_STAGING",
      "line-r3a-5": "LEFT_LAUNCH_CATCH",
      "line-r3a-8": "RIGHT_LAUNCH_CATCH",
      "line-r3a-10": "SCORING_FLOWER_STAGING",
      "line-r3a-12": "LEFT_LAUNCH_CATCH"
    },
    "cards": [
      {
        "id": "c-r3a-1",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-r3a-2",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-r3a-3",
        "kind": "path",
        "lineId": "line-r3a-3",
        "park": false
      },
      {
        "id": "c-r3a-4",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 8
      },
      {
        "id": "c-r3a-5",
        "kind": "path",
        "lineId": "line-r3a-5",
        "park": false
      },
      {
        "id": "c-r3a-6",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-r3a-14",
        "kind": "firstOf",
        "label": "Did TIP 2 come?",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": [
              {
                "id": "c-r3a-7",
                "kind": "firstOf",
                "label": "Catch the TIP 2 spill",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 2000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "c-r3a-8",
                "kind": "path",
                "lineId": "line-r3a-8",
                "park": false
              },
              {
                "id": "c-r3a-9",
                "kind": "action",
                "name": "LaunchAll"
              }
            ]
          },
          {
            "afterMs": 2500,
            "cards": [
              {
                "id": "c-r3a-10",
                "kind": "path",
                "lineId": "line-r3a-10",
                "park": false
              },
              {
                "id": "c-r3a-11",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 8
              },
              {
                "id": "c-r3a-12",
                "kind": "path",
                "lineId": "line-r3a-12",
                "park": false
              },
              {
                "id": "c-r3a-13",
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
      "LEFT_LAUNCH_CATCH",
      "RIGHT_LAUNCH_CATCH",
      "RIGHT_START",
      "SCORING_FLOWER_STAGING",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "R-3Tip-A"
  }
}