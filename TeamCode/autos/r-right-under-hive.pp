{
  "version": "2.0.0",
  "timestamp": "2026-11-19T19:52:33.483Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "locked": false,
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "r-ru-open",
      "color": "#4e79a7",
      "name": "to RIGHT_AUTO_HIVE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.2,
        "y": 20.9
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 96.1
      }
    },
    {
      "id": "r-ru-wait-park",
      "color": "#4e79a7",
      "name": "RIGHT_AUTO_HIVE, corridor, to LEFT_PARK (guard)",
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
          "x": 61,
          "y": 30
        },
        {
          "x": 61,
          "y": 30
        },
        {
          "x": 61,
          "y": 30
        },
        {
          "x": 61,
          "y": 55
        },
        {
          "x": 61,
          "y": 55
        },
        {
          "x": 61,
          "y": 55
        },
        {
          "x": 61,
          "y": 80
        },
        {
          "x": 61,
          "y": 80
        },
        {
          "x": 61,
          "y": 80
        },
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 57,
          "y": 108
        },
        {
          "x": 40,
          "y": 110
        },
        {
          "x": 26,
          "y": 112
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.05,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 96.1,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.05,
              "endProgress": 0.72,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.72,
              "endProgress": 0.95,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.95,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-1",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 59.2,
        "y": 20.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 96.10000000000002
      }
    },
    {
      "id": "r-ru-garden",
      "color": "#4e79a7",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 96.1,
        "endDeg": 270
      }
    },
    {
      "id": "r-ru-back",
      "color": "#4e79a7",
      "name": "GARDEN back to RIGHT_AUTO_HIVE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.2,
        "y": 20.9
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 96.1
      }
    },
    {
      "id": "r-ru-garden-park",
      "color": "#4e79a7",
      "name": "GARDEN, corridor, to LEFT_PARK (guard)",
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
          "x": 30,
          "y": 12
        },
        {
          "x": 61,
          "y": 12
        },
        {
          "x": 61,
          "y": 30
        },
        {
          "x": 61,
          "y": 30
        },
        {
          "x": 61,
          "y": 30
        },
        {
          "x": 61,
          "y": 55
        },
        {
          "x": 61,
          "y": 55
        },
        {
          "x": 61,
          "y": 55
        },
        {
          "x": 61,
          "y": 80
        },
        {
          "x": 61,
          "y": 80
        },
        {
          "x": 61,
          "y": 80
        },
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 57,
          "y": 108
        },
        {
          "x": 40,
          "y": 110
        },
        {
          "x": 26,
          "y": 112
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.08,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 450
              }
            },
            {
              "startProgress": 0.08,
              "endProgress": 0.74,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.74,
              "endProgress": 0.95,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.95,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
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
        "x": 59.2,
        "y": 20.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 96.10000000000002
      }
    },
    {
      "id": "r-ru-up",
      "color": "#4e79a7",
      "name": "up the corridor under the HIVE to R_HIVE_EXIT",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60.5,
        "y": 100
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 96.1,
        "endDeg": 90
      }
    },
    {
      "id": "r-ru-exit-park",
      "color": "#4e79a7",
      "name": "R_HIVE_EXIT west above the HIVE to LEFT_PARK",
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
          "x": 52,
          "y": 101
        },
        {
          "x": 38,
          "y": 101
        },
        {
          "x": 28,
          "y": 101
        },
        {
          "x": 22,
          "y": 106
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.95,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.95,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-3",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 59.2,
        "y": 20.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 96.10000000000002
      }
    },
    {
      "id": "r-ru-up-fix",
      "color": "#4e79a7",
      "name": "fix: up the corridor under the HIVE to R_HIVE_EXIT",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60.5,
        "y": 100
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 96.1,
        "endDeg": 90
      }
    },
    {
      "id": "r-ru-fix",
      "color": "#4e79a7",
      "name": "R_HIVE_EXIT round to R_LEFT_FIX",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61.5,
        "y": 114
      },
      "controlPoints": [
        {
          "x": 55,
          "y": 103
        },
        {
          "x": 60,
          "y": 106
        },
        {
          "x": 62,
          "y": 110
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.1,
              "endProgress": 0.45,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 268.4
              }
            },
            {
              "startProgress": 0.45,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 268.4
              }
            }
          ]
        }
      }
    },
    {
      "id": "r-ru-exit-park-2",
      "color": "#4e79a7",
      "name": "R_HIVE_EXIT west above the HIVE to LEFT_PARK (instead of the fix)",
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
          "x": 52,
          "y": 101
        },
        {
          "x": 38,
          "y": 101
        },
        {
          "x": 28,
          "y": 101
        },
        {
          "x": 22,
          "y": 106
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.95,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.95,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-4",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.5,
        "y": 114
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": -91.6
      }
    },
    {
      "id": "r-ru-fix-park",
      "color": "#4e79a7",
      "name": "R_LEFT_FIX to LEFT_PARK",
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
          "x": 44,
          "y": 113
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.4,
        "endDeg": 360
      }
    },
    {
      "id": "link-5",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.5,
        "y": 114
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": -91.6
      }
    },
    {
      "id": "r-ru-fix-skip-park",
      "color": "#4e79a7",
      "name": "LEFT down, R_LEFT_FIX to LEFT_PARK",
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
          "x": 44,
          "y": 113
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.4,
        "endDeg": 360
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
      "lineId": "r-ru-open"
    },
    {
      "kind": "path",
      "lineId": "r-ru-wait-park"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "r-ru-garden"
    },
    {
      "kind": "path",
      "lineId": "r-ru-back"
    },
    {
      "kind": "path",
      "lineId": "r-ru-garden-park"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "r-ru-up"
    },
    {
      "kind": "path",
      "lineId": "r-ru-exit-park"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "r-ru-up-fix"
    },
    {
      "kind": "path",
      "lineId": "r-ru-fix"
    },
    {
      "kind": "path",
      "lineId": "r-ru-exit-park-2"
    },
    {
      "kind": "path",
      "lineId": "link-4"
    },
    {
      "kind": "path",
      "lineId": "r-ru-fix-park"
    },
    {
      "kind": "path",
      "lineId": "link-5"
    },
    {
      "kind": "path",
      "lineId": "r-ru-fix-skip-park"
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
        "PickFlower": 4,
        "GardenGather": 3,
        "LaunchOne": 0.5,
        "SpinUp": 0.1
      }
    },
    "points": {
      "GARDEN_STAGING": [
        11.5,
        22.1,
        270
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ],
      "RIGHT_AUTO_HIVE": [
        59.2,
        20.9,
        96.1
      ],
      "RIGHT_START": [
        55.6,
        7.7,
        90
      ],
      "R_HIVE_EXIT": [
        60.5,
        100,
        90
      ],
      "R_LEFT_FIX": [
        61.5,
        114,
        268.4
      ]
    },
    "pathEnds": {
      "r-ru-open": "RIGHT_AUTO_HIVE",
      "r-ru-garden": "GARDEN_STAGING",
      "r-ru-back": "RIGHT_AUTO_HIVE",
      "r-ru-up": "R_HIVE_EXIT",
      "r-ru-up-fix": "R_HIVE_EXIT",
      "r-ru-exit-park": "LEFT_PARK",
      "r-ru-exit-park-2": "LEFT_PARK",
      "r-ru-wait-park": "LEFT_PARK",
      "r-ru-garden-park": "LEFT_PARK",
      "r-ru-fix": "R_LEFT_FIX",
      "r-ru-fix-park": "LEFT_PARK",
      "r-ru-fix-skip-park": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "c-r-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-r-open",
        "kind": "path",
        "lineId": "r-ru-open",
        "park": false
      },
      {
        "id": "c-r-pre",
        "kind": "action",
        "name": "LaunchAll",
        "timeoutS": 1
      },
      {
        "id": "c-r-garden",
        "kind": "path",
        "lineId": "r-ru-garden",
        "park": false
      },
      {
        "id": "c-r-gather",
        "kind": "action",
        "name": "GardenGather",
        "timeoutS": 5.5
      },
      {
        "id": "c-r-back",
        "kind": "path",
        "lineId": "r-ru-back",
        "park": false
      },
      {
        "id": "c-r-wait",
        "kind": "firstOf",
        "label": "RIGHT up (TIP 2, or no TIP 1) before 18 s, otherwise fix LEFT",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [
              {
                "id": "c-r-right",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "c-r-up",
                "kind": "path",
                "lineId": "r-ru-up",
                "park": false
              },
              {
                "id": "c-r-park",
                "kind": "path",
                "lineId": "r-ru-exit-park",
                "park": true
              }
            ]
          },
          {
            "afterMs": 18000,
            "matchClock": true,
            "adjustable": true,
            "tip": 2,
            "cards": [
              {
                "id": "c-r-up-fix",
                "kind": "path",
                "lineId": "r-ru-up-fix",
                "park": false
              },
              {
                "id": "c-r-fix",
                "kind": "path",
                "lineId": "r-ru-fix",
                "park": false
              },
              {
                "id": "c-r-left-check",
                "kind": "firstOf",
                "label": "LEFT up?",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": [
                      {
                        "id": "c-r-left",
                        "kind": "action",
                        "name": "LaunchAll",
                        "timeoutS": 1
                      },
                      {
                        "id": "c-r-fix-park",
                        "kind": "path",
                        "lineId": "r-ru-fix-park",
                        "park": true
                      }
                    ]
                  },
                  {
                    "afterMs": 750,
                    "cards": [
                      {
                        "id": "c-r-fix-skip-park",
                        "kind": "path",
                        "lineId": "r-ru-fix-skip-park",
                        "park": true
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ],
        "alongside": "SpinUp"
      }
    ],
    "startAt": "RIGHT_START",
    "linkPaths": [
      "link-1",
      "link-2",
      "link-3",
      "link-4",
      "link-5"
    ],
    "parkFrom": {
      "c-r-open": "r-ru-wait-park",
      "c-r-garden": "r-ru-wait-park",
      "c-r-back": "r-ru-garden-park",
      "c-r-up": "r-ru-wait-park",
      "c-r-up-fix": "r-ru-wait-park",
      "c-r-fix": "r-ru-exit-park-2"
    },
    "teamSpots": [
      "GARDEN_STAGING",
      "LEFT_PARK",
      "RIGHT_AUTO_HIVE",
      "RIGHT_START"
    ],
    "exportName": "R-RightUnderHive"
  }
}
