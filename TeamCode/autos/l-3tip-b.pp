{
  "version": "2.0.0",
  "timestamp": "2026-11-04T12:00:00.000Z",
  "startPoint": {
    "x": 62.3,
    "y": 133.8,
    "locked": false,
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "line-l3b-2",
      "color": "#59a14f",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 273.4
      }
    },
    {
      "id": "line-l3b-4",
      "color": "#59a14f",
      "name": "centre lane to GARDEN_STAGING",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 11.5,
        "y": 22.1
      },
      "controlPoints": [
        {
          "x": 63.5,
          "y": 73.7
        },
        {
          "x": 55.3,
          "y": 40.0
        },
        {
          "x": 64.6,
          "y": 35.6
        },
        {
          "x": 51.1,
          "y": 34.4
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.18,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 273.4,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.18,
              "endProgress": 0.78,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.78,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "line-l3b-6",
      "color": "#59a14f",
      "name": "to RIGHT_LAUNCH_CATCH",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 91.9
      }
    },
    {
      "id": "line-l3b-9",
      "color": "#59a14f",
      "name": "centre lane to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 55.4,
          "y": 83.8
        },
        {
          "x": 63.9,
          "y": 93.3
        },
        {
          "x": 59.8,
          "y": 95.6
        },
        {
          "x": 62.3,
          "y": 106.9
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.21,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 91.9,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.21,
              "endProgress": 0.6,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "line-l3b-10",
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
      "id": "line-l3b-12",
      "color": "#59a14f",
      "name": "centre lane to LEFT_PARK ",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 55.4,
          "y": 83.8
        },
        {
          "x": 63.9,
          "y": 93.3
        },
        {
          "x": 59.8,
          "y": 95.6
        },
        {
          "x": 62.3,
          "y": 106.9
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.21,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 91.9,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.21,
              "endProgress": 0.6,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            }
          ]
        }
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
      "lineId": "line-l3b-2"
    },
    {
      "kind": "path",
      "lineId": "line-l3b-4"
    },
    {
      "kind": "path",
      "lineId": "line-l3b-6"
    },
    {
      "kind": "path",
      "lineId": "line-l3b-9"
    },
    {
      "kind": "path",
      "lineId": "line-l3b-10"
    },
    {
      "kind": "path",
      "lineId": "line-l3b-12"
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
      "LEFT_START": [
        62.3,
        133.8,
        270
      ],
      "LEFT_LAUNCH_CATCH": [
        57.5,
        123.5,
        273.4
      ],
      "GARDEN_STAGING": [
        11.5,
        22.1,
        270
      ],
      "RIGHT_LAUNCH_CATCH": [
        57.5,
        18,
        91.9
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ]
    },
    "pathEnds": {
      "line-l3b-2": "LEFT_LAUNCH_CATCH",
      "line-l3b-4": "GARDEN_STAGING",
      "line-l3b-6": "RIGHT_LAUNCH_CATCH",
      "line-l3b-9": "LEFT_PARK",
      "line-l3b-10": "RIGHT_LAUNCH_CATCH",
      "line-l3b-12": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "c-l3b-1",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-l3b-2",
        "kind": "path",
        "lineId": "line-l3b-2",
        "park": false
      },
      {
        "id": "c-l3b-13",
        "kind": "firstOf",
        "label": "Wait for TIP or CELL up",
        "rows": [
          {
            "when": [
              "CellUp"
            ],
            "cards": [
              {
                "id": "c-l3b-3",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-l3b-4",
                "kind": "path",
                "lineId": "line-l3b-4",
                "park": false
              },
              {
                "id": "c-l3b-5",
                "kind": "action",
                "name": "GardenGather",
                "timeoutS": 8
              },
              {
                "id": "c-l3b-6",
                "kind": "path",
                "lineId": "line-l3b-6",
                "park": false
              },
              {
                "id": "c-l3b-7",
                "kind": "firstOf",
                "label": "Wait for RIGHT up (TIP 2)",
                "rows": [
                  {
                    "when": [
                      "CellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 4000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "c-l3b-8",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-l3b-9",
                "kind": "path",
                "lineId": "line-l3b-9",
                "park": true
              }
            ]
          },
          {
            "afterMs": 10000,
            "cards": [
              {
                "id": "c-l3b-10",
                "kind": "path",
                "lineId": "line-l3b-10",
                "park": false
              },
              {
                "id": "c-l3b-11",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-l3b-12",
                "kind": "path",
                "lineId": "line-l3b-12",
                "park": true
              }
            ],
            "matchClock": true,
            "adjustable": true
          }
        ]
      }
    ],
    "startAt": "LEFT_START",
    "teamSpots": [
      "GARDEN_STAGING",
      "LEFT_LAUNCH_CATCH",
      "LEFT_PARK",
      "LEFT_START",
      "RIGHT_LAUNCH_CATCH"
    ],
    "exportName": "L-3Tip-B"
  }
}