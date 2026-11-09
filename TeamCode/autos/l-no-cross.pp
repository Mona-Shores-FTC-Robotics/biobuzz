{
  "version": "2.0.0",
  "timestamp": "2026-11-06T01:03:03.647Z",
  "startPoint": {
    "x": 62.3,
    "y": 133.8,
    "locked": false,
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "line-mw0c4s2s-i6yeu6",
      "color": "#76b7b2",
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
      "id": "line-mw0c4s32-8noomj",
      "color": "#9c755f",
      "name": "LEFT_LAUNCH_CATCH park",
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
        "startDeg": 273.4,
        "endDeg": 0
      }
    },
    {
      "id": "link-1",
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
      "id": "line-mw0eq8bt-go9gl6",
      "color": "#b07aa1",
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
          "x": 56.76137344482316,
          "y": 93.46014040136694
        },
        {
          "x": 68.26574509695736,
          "y": 72.987287864473
        },
        {
          "x": 58.69963001513299,
          "y": 26.400462127763195
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 273.4,
        "endDeg": 90,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 273.4,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.2,
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
      "name": "RIGHT_LAUNCH_CATCH park",
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
          "x": 28,
          "y": 25
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 91.9,
        "endDeg": 0
      }
    },
    {
      "id": "link-2",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 56.1,
        "y": 26
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "line-mw0eyliu-i7tra3",
      "color": "#9c755f",
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
      "id": "line-mw0rkae5-9j07qg",
      "color": "#76b7b2",
      "name": "WALL_FLOWER_STAGING park",
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
        "startDeg": 180,
        "endDeg": 0
      }
    },
    {
      "id": "link-3",
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
      "id": "line-mw0f0i5g-sowt7p",
      "color": "#76b7b2",
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
          "x": 39.35072361062994,
          "y": 59.89970029346436
        },
        {
          "x": 25.497455470737915,
          "y": 127.60056392485252
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
              "endProgress": 0.5,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 180,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 90,
                "endDeg": 273.4
              }
            }
          ]
        }
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
        "startDeg": 273.4,
        "endDeg": 90
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
      "id": "link-4",
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
      "id": "line-mw0c4s2t-m8fs6x",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 273.4
      }
    },
    {
      "id": "line-mw0c4s2u-8wbzmf",
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
        "startDeg": 273.4,
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
      "lineId": "line-mw0c4s2s-i6yeu6"
    },
    {
      "kind": "path",
      "lineId": "line-mw0c4s32-8noomj"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "line-mw0eq8bt-go9gl6"
    },
    {
      "kind": "path",
      "lineId": "line-mw0rkae5-kh7tv8"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "line-mw0eyliu-i7tra3"
    },
    {
      "kind": "path",
      "lineId": "line-mw0rkae5-9j07qg"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "line-mw0f0i5g-sowt7p"
    },
    {
      "kind": "path",
      "lineId": "line-mw0c4s2t-ne8h5h"
    },
    {
      "kind": "path",
      "lineId": "line-mw0c4s32-ave4gg"
    },
    {
      "kind": "path",
      "lineId": "link-4"
    },
    {
      "kind": "path",
      "lineId": "line-mw0c4s2t-m8fs6x"
    },
    {
      "kind": "path",
      "lineId": "line-mw0c4s2u-8wbzmf"
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
        "CameraBlind"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 4.5,
        "PickFlower": 4.0,
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
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ],
      "RIGHT_LAUNCH_CATCH": [
        57.5,
        18,
        91.9
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
      "RIGHT_LAUNCH_CLOSE": [
        56.1,
        26,
        90
      ]
    },
    "pathEnds": {
      "line-mw0c4s2s-i6yeu6": "LEFT_LAUNCH_CATCH",
      "line-mw0c4s2t-ne8h5h": "SCORING_FLOWER_STAGING",
      "line-mw0c4s2t-m8fs6x": "LEFT_LAUNCH_CATCH",
      "line-mw0c4s2u-8wbzmf": "LEFT_PARK",
      "line-mw0c4s32-8noomj": "LEFT_PARK",
      "line-mw0c4s32-ave4gg": "LEFT_PARK",
      "line-mw0eq8bt-go9gl6": "RIGHT_LAUNCH_CLOSE",
      "line-mw0eyliu-i7tra3": "WALL_FLOWER_STAGING",
      "line-mw0f0i5g-sowt7p": "LEFT_LAUNCH_CATCH",
      "line-mw0rkae5-kh7tv8": "LEFT_PARK",
      "line-mw0rkae5-9j07qg": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "c-mw0c4s2r-jo6tm",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-mw0c4s2s-oo8yq",
        "kind": "path",
        "lineId": "line-mw0c4s2s-i6yeu6",
        "park": false
      },
      {
        "id": "c-mw0c4s2s-ptffv",
        "kind": "firstOf",
        "label": "Wait for TIP or CELL up",
        "rows": [
          {
            "when": [
              "CellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 8000,
            "matchClock": true,
            "adjustable": true,
            "cards": [
              {
                "id": "c-mw0eq8bt-2vuzy",
                "kind": "path",
                "lineId": "line-mw0eq8bt-go9gl6",
                "park": false
              },
              {
                "id": "c-mw0eygqv-43e96",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-mw0eyliu-eojd4",
                "kind": "path",
                "lineId": "line-mw0eyliu-i7tra3",
                "park": false
              },
              {
                "id": "c-mw0eypgg-196uj",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 8
              },
              {
                "id": "c-mw0f0i5g-qi1es",
                "kind": "path",
                "lineId": "line-mw0f0i5g-sowt7p",
                "park": false
              }
            ]
          }
        ],
        "alongside": "SpinUp"
      },
      {
        "id": "c-mw0c4s2t-8epji",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-mw0c4s2t-jqsjs",
        "kind": "path",
        "lineId": "line-mw0c4s2t-ne8h5h",
        "park": false
      },
      {
        "id": "c-mw0c4s2t-sfdhk",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 8
      },
      {
        "id": "c-mw0c4s2t-lhsjw",
        "kind": "path",
        "lineId": "line-mw0c4s2t-m8fs6x",
        "park": false
      },
      {
        "id": "c-mw0c4s2u-axth1",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-mw0c4s2u-u2yl0",
        "kind": "path",
        "lineId": "line-mw0c4s2u-8wbzmf",
        "park": true
      }
    ],
    "startAt": "LEFT_START",
    "linkPaths": [
      "link-1",
      "link-2",
      "link-3",
      "link-4"
    ],
    "parkFrom": {
      "c-mw0c4s2s-oo8yq": "line-mw0c4s32-8noomj",
      "c-mw0c4s2t-jqsjs": "line-mw0c4s32-ave4gg",
      "c-mw0eq8bt-2vuzy": "line-mw0rkae5-kh7tv8",
      "c-mw0eyliu-eojd4": "line-mw0rkae5-9j07qg",
      "c-mw0f0i5g-qi1es": "line-mw0c4s32-8noomj"
    },
    "teamSpots": [
      "LEFT_LAUNCH_CATCH",
      "LEFT_LAUNCH_OFFSET",
      "LEFT_PARK",
      "LEFT_START",
      "RIGHT_LAUNCH_CATCH",
      "RIGHT_LAUNCH_CLOSE",
      "SCORING_FLOWER_STAGING",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "L-NoCross"
  }
}