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
      "id": "to-garden",
      "color": "#59a14f",
      "name": "to GARDEN_STAGING",
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
          "x": 40,
          "y": 20
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 90,
        "endDeg": 270,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 90,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "garden-park",
      "color": "#f28e2b",
      "name": "GARDEN_STAGING park",
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
          "x": 22,
          "y": 60
        },
        {
          "x": 24,
          "y": 105
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 0
      }
    },
    {
      "id": "link-1",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 11.5,
        "y": 22.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-offset",
      "color": "#b07aa1",
      "name": "to RIGHT_PAIR_CATCH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61.0,
        "y": 13.0
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "startDeg": 270,
        "endDeg": 96.2,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.7,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 270,
                "endDeg": 96.2
              }
            },
            {
              "startProgress": 0.7,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 96.2,
                "endDeg": 96.2
              }
            }
          ]
        }
      }
    },
    {
      "id": "offset-park",
      "color": "#edc948",
      "name": "under the HIVE to LEFT_PARK",
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
          "x": 60,
          "y": 40
        },
        {
          "x": 58,
          "y": 70
        },
        {
          "x": 58,
          "y": 100
        },
        {
          "x": 76,
          "y": 138
        },
        {
          "x": 26,
          "y": 120
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 96.2,
        "endDeg": 0,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 96.2,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.1,
              "endProgress": 0.7,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 90,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.7,
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
      "id": "link-2",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.0,
        "y": 13.0
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 96.2
      }
    },
    {
      "id": "rescue-under",
      "color": "#e15759",
      "name": "under the HIVE to HIVE_NORTH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 58.5,
        "y": 100.5
      },
      "controlPoints": [
        {
          "x": 60,
          "y": 40
        },
        {
          "x": 58.5,
          "y": 70
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 96.2,
        "endDeg": 90,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 96.2,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.1,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 90,
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "rescue-west",
      "color": "#4e79a7",
      "name": "sideways to RESCUE_TURN",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 26,
        "y": 100.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "rescue-park",
      "color": "#9c755f",
      "name": "RESCUE_TURN to LEFT_PARK",
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
          "x": 13,
          "y": 99
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 90,
        "endDeg": 0,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
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
      "lineId": "to-garden"
    },
    {
      "kind": "path",
      "lineId": "garden-park"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "to-offset"
    },
    {
      "kind": "path",
      "lineId": "offset-park"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "rescue-under"
    },
    {
      "kind": "path",
      "lineId": "rescue-west"
    },
    {
      "kind": "path",
      "lineId": "rescue-park"
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
      "RIGHT_PAIR_CATCH": [
        61.0,
        13.0,
        96.2
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ],
      "HIVE_NORTH": [
        58.5,
        100.5,
        90
      ],
      "RESCUE_TURN": [
        26,
        100.5,
        90
      ],
      "GARDEN_STAGING": [
        11.5,
        22.1,
        270
      ]
    },
    "pathEnds": {
      "to-garden": "GARDEN_STAGING",
      "garden-park": "LEFT_PARK",
      "to-offset": "RIGHT_PAIR_CATCH",
      "offset-park": "LEFT_PARK",
      "rescue-under": "HIVE_NORTH",
      "rescue-west": "RESCUE_TURN",
      "rescue-park": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "c-a-1",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-a-2",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-p-3",
        "kind": "path",
        "lineId": "to-garden",
        "park": false
      },
      {
        "id": "c-a-4",
        "kind": "action",
        "name": "GardenGather",
        "timeoutS": 7
      },
      {
        "id": "c-p-5",
        "kind": "path",
        "lineId": "to-offset",
        "park": false
      },
      {
        "id": "c-w-14",
        "kind": "firstOf",
        "label": "RIGHT still up before 16 s: TIP 1 did not come",
        "rows": [
          {
            "afterMs": 16000,
            "matchClock": true,
            "cards": [
              {
                "id": "c-w-9",
                "kind": "firstOf",
                "label": "RIGHT up before 25 s, otherwise park",
                "rows": [
                  {
                    "afterMs": 25000,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "c-p-6",
                        "kind": "path",
                        "lineId": "offset-park",
                        "park": true
                      }
                    ]
                  },
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": [
                      {
                        "id": "c-a-7",
                        "kind": "action",
                        "name": "LaunchAll"
                      },
                      {
                        "id": "c-p-8",
                        "kind": "path",
                        "lineId": "offset-park",
                        "park": true
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [
              {
                "id": "c-a-10",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-p-11",
                "kind": "path",
                "lineId": "rescue-under",
                "park": false
              },
              {
                "id": "c-p-12",
                "kind": "path",
                "lineId": "rescue-west",
                "park": false
              },
              {
                "id": "c-p-13",
                "kind": "path",
                "lineId": "rescue-park",
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
      "c-p-3": "garden-park",
      "c-p-5": "offset-park"
    },
    "teamSpots": [
      "GARDEN_STAGING",
      "LEFT_PARK",
      "RIGHT_START"
    ],
    "exportName": "R-Pair-Garden"
  }
}
