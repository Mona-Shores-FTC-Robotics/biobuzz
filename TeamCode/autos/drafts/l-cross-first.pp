{
  "version": "2.0.0",
  "timestamp": "2026-11-25T14:52:59.000Z",
  "startPoint": {
    "x": 62.3,
    "y": 133.8,
    "locked": false,
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "line-lcf-cross",
      "color": "#b07aa1",
      "name": "LEFT_START under the HIVE to RIGHT_AUTO_FIX_TIP",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55.3,
        "y": 25.7
      },
      "controlPoints": [
        {
          "x": 52,
          "y": 100
        },
        {
          "x": 68.26574509695736,
          "y": 72.987287864473
        },
        {
          "x": 58.7,
          "y": 27
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
              "endProgress": 0.05,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 270,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.05,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 270,
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
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "line-mw0rkae5-kh7tv8",
      "color": "#4e79a7",
      "name": "RIGHT_AUTO_FIX_TIP park under the HIVE",
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
          "y": 50
        },
        {
          "x": 66,
          "y": 80
        },
        {
          "x": 65,
          "y": 105
        },
        {
          "x": 62,
          "y": 122
        },
        {
          "x": 40,
          "y": 122
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
              "endProgress": 0.75,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 90,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.75,
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
      "id": "link-1",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 55.3,
        "y": 25.7
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "line-lnc-fixtip-to-scoring-flower",
      "color": "#4e79a7",
      "name": "under the HIVE to SCORING_FLOWER_STAGING",
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
      "controlPoints": [
        {
          "x": 58.7,
          "y": 40
        },
        {
          "x": 66,
          "y": 73
        },
        {
          "x": 60,
          "y": 102
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "line-mw0c4s32-ave4gg",
      "color": "#e15759",
      "name": "SCORING_FLOWER_STAGING park",
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
      "controlPoints": [],
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
      "id": "line-mw0c4s2t-m8fs6x",
      "color": "#f28e2b",
      "name": "to LEFT_AUTO_HIVE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 120.45
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 268.9
      }
    },
    {
      "id": "line-mw0c4s32-8noomj",
      "color": "#9c755f",
      "name": "LEFT_AUTO_HIVE park",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 268.1,
        "endDeg": 0
      }
    },
    {
      "id": "link-3",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 120.45
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "line-lcf-park-1",
      "color": "#9c755f",
      "name": "to LEFT_PARK",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 0
      }
    },
    {
      "id": "link-4",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 120.45
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "line-lcf-park-2",
      "color": "#9c755f",
      "name": "to LEFT_PARK",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 0
      }
    },
    {
      "id": "link-5",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 55.3,
        "y": 25.7
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "line-mwbksy73-ypsvit",
      "color": "#4e79a7",
      "name": "to LEFT_AUTO_HIVE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 120.45
      },
      "controlPoints": [
        {
          "x": 58.7,
          "y": 27
        },
        {
          "x": 68.26574509695736,
          "y": 72.987287864473
        },
        {
          "x": 52,
          "y": 94
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 90,
        "endDeg": 268.9,
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
                "startDeg": 90,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.7,
              "endProgress": 0.92,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 90,
                "endDeg": 268.9
              }
            },
            {
              "startProgress": 0.92,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 268.9,
                "endDeg": 268.9
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-6",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 120.45
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "line-mw0c4s2t-ne8h5h",
      "color": "#f28e2b",
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
        "startDeg": 268.9,
        "endDeg": 90
      }
    },
    {
      "id": "link-7",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 120.45
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "line-lcf-park-3",
      "color": "#9c755f",
      "name": "to LEFT_PARK",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 0
      }
    },
    {
      "id": "link-8",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 120.45
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "line-lcf-park-4",
      "color": "#9c755f",
      "name": "to LEFT_PARK",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 0
      }
    },
    {
      "id": "link-9",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 120.45
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "line-lcf-park-5",
      "color": "#9c755f",
      "name": "to LEFT_PARK",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
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
      "lineId": "line-lcf-cross"
    },
    {
      "kind": "path",
      "lineId": "line-mw0rkae5-kh7tv8"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "line-lnc-fixtip-to-scoring-flower"
    },
    {
      "kind": "path",
      "lineId": "line-mw0c4s32-ave4gg"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "line-mw0c4s2t-m8fs6x"
    },
    {
      "kind": "path",
      "lineId": "line-mw0c4s32-8noomj"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "line-lcf-park-1"
    },
    {
      "kind": "path",
      "lineId": "link-4"
    },
    {
      "kind": "path",
      "lineId": "line-lcf-park-2"
    },
    {
      "kind": "path",
      "lineId": "link-5"
    },
    {
      "kind": "path",
      "lineId": "line-mwbksy73-ypsvit"
    },
    {
      "kind": "path",
      "lineId": "link-6"
    },
    {
      "kind": "path",
      "lineId": "line-mw0c4s2t-ne8h5h"
    },
    {
      "kind": "path",
      "lineId": "link-7"
    },
    {
      "kind": "path",
      "lineId": "line-lcf-park-3"
    },
    {
      "kind": "path",
      "lineId": "link-8"
    },
    {
      "kind": "path",
      "lineId": "line-lcf-park-4"
    },
    {
      "kind": "path",
      "lineId": "link-9"
    },
    {
      "kind": "path",
      "lineId": "line-lcf-park-5"
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
        "LaunchAll": 4.5,
        "PickFlower": 4,
        "GardenGather": 5,
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
      "LEFT_AUTO_HIVE": [
        61.4,
        120.45,
        268.9
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ],
      "RIGHT_AUTO_FIX_TIP": [
        55.3,
        25.7,
        90
      ]
    },
    "pathEnds": {
      "line-lcf-cross": "RIGHT_AUTO_FIX_TIP",
      "line-mw0rkae5-kh7tv8": "LEFT_PARK",
      "line-lnc-fixtip-to-scoring-flower": "SCORING_FLOWER_STAGING",
      "line-mw0c4s32-ave4gg": "LEFT_PARK",
      "line-mw0c4s2t-m8fs6x": "LEFT_AUTO_HIVE",
      "line-mw0c4s32-8noomj": "LEFT_PARK",
      "line-lcf-park-1": "LEFT_PARK",
      "line-lcf-park-2": "LEFT_PARK",
      "line-mwbksy73-ypsvit": "LEFT_AUTO_HIVE",
      "line-mw0c4s2t-ne8h5h": "SCORING_FLOWER_STAGING",
      "line-lcf-park-3": "LEFT_PARK",
      "line-lcf-park-4": "LEFT_PARK",
      "line-lcf-park-5": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "c-lcf-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-lcf-cross",
        "kind": "path",
        "lineId": "line-lcf-cross",
        "park": false
      },
      {
        "id": "c-lcf-right-up",
        "kind": "firstOf",
        "label": "RIGHT CELL up?",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [
              {
                "id": "c-lcf-launch-right",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-lcf-fixtip-sf",
                "kind": "path",
                "lineId": "line-lnc-fixtip-to-scoring-flower",
                "park": false
              },
              {
                "id": "c-lcf-pick",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 8
              },
              {
                "id": "c-lcf-left",
                "kind": "path",
                "lineId": "line-mw0c4s2t-m8fs6x",
                "park": false
              },
              {
                "id": "c-lcf-left-up",
                "kind": "firstOf",
                "label": "LEFT CELL up?",
                "rows": [
                  {
                    "when": [
                      "CellUp"
                    ],
                    "cards": [
                      {
                        "id": "c-lcf-launch-left",
                        "kind": "action",
                        "name": "LaunchAll"
                      },
                      {
                        "id": "c-lcf-park-shot",
                        "kind": "path",
                        "lineId": "line-lcf-park-1",
                        "park": true
                      }
                    ]
                  },
                  {
                    "afterMs": 23000,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "c-lcf-left-up-timeout",
                        "kind": "path",
                        "lineId": "line-lcf-park-2",
                        "park": true
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            "afterMs": 750,
            "label": "TIP came after all",
            "cards": [
              {
                "id": "c-lcf-back",
                "kind": "path",
                "lineId": "line-mwbksy73-ypsvit",
                "park": false
              },
              {
                "id": "c-lcf-late-up",
                "kind": "firstOf",
                "label": "LEFT CELL up?",
                "rows": [
                  {
                    "when": [
                      "CellUp"
                    ],
                    "cards": [
                      {
                        "id": "c-lcf-late-launch",
                        "kind": "action",
                        "name": "LaunchAll"
                      },
                      {
                        "id": "c-lcf-late-sf",
                        "kind": "path",
                        "lineId": "line-mw0c4s2t-ne8h5h",
                        "park": false
                      },
                      {
                        "id": "c-lcf-late-pick",
                        "kind": "action",
                        "name": "PickFlower",
                        "timeoutS": 8
                      },
                      {
                        "id": "c-lcf-late-left",
                        "kind": "path",
                        "lineId": "line-mw0c4s2t-m8fs6x",
                        "park": false
                      },
                      {
                        "id": "c-lcf-late-back-up",
                        "kind": "firstOf",
                        "label": "LEFT CELL back up?",
                        "rows": [
                          {
                            "when": [
                              "CellUp"
                            ],
                            "cards": [
                              {
                                "id": "c-lcf-late-launch-2",
                                "kind": "action",
                                "name": "LaunchAll"
                              },
                              {
                                "id": "c-lcf-late-park-shot",
                                "kind": "path",
                                "lineId": "line-lcf-park-3",
                                "park": true
                              }
                            ]
                          },
                          {
                            "afterMs": 23000,
                            "matchClock": true,
                            "cards": [
                              {
                                "id": "c-lcf-late-back-up-timeout",
                                "kind": "path",
                                "lineId": "line-lcf-park-4",
                                "park": true
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "afterMs": 23000,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "c-lcf-late-up-timeout",
                        "kind": "path",
                        "lineId": "line-lcf-park-5",
                        "park": true
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    ],
    "startAt": "LEFT_START",
    "linkPaths": [
      "link-1",
      "link-2",
      "link-3",
      "link-4",
      "link-5",
      "link-6",
      "link-7",
      "link-8",
      "link-9"
    ],
    "parkFrom": {
      "c-lcf-cross": "line-mw0rkae5-kh7tv8",
      "c-lcf-fixtip-sf": "line-mw0c4s32-ave4gg",
      "c-lcf-left": "line-mw0c4s32-8noomj",
      "c-lcf-back": "line-mw0c4s32-8noomj",
      "c-lcf-late-sf": "line-mw0c4s32-ave4gg",
      "c-lcf-late-left": "line-mw0c4s32-8noomj"
    },
    "teamSpots": [
      "LEFT_AUTO_HIVE",
      "LEFT_PARK",
      "LEFT_START",
      "RIGHT_AUTO_FIX_TIP",
      "SCORING_FLOWER_STAGING"
    ],
    "exportName": "L-CrossFirst"
  }
}
