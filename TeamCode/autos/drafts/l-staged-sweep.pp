{
  "version": "2.0.0",
  "timestamp": "2026-11-23T20:51:13.920Z",
  "startPoint": {
    "x": 34.2,
    "y": 130.8,
    "locked": false,
    "headingDeg": 295
  },
  "lines": [
    {
      "id": "l2",
      "color": "#76b7b2",
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
        "startDeg": 295,
        "endDeg": 450
      }
    },
    {
      "id": "p1",
      "color": "#f28e2b",
      "name": "STAGE_START park",
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
        "startDeg": 295,
        "endDeg": 360
      }
    },
    {
      "id": "link-1",
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
      "id": "l3",
      "color": "#59a14f",
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
      "id": "p3",
      "color": "#edc948",
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
        "startDeg": 268.9,
        "endDeg": 360
      }
    },
    {
      "id": "link-2",
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
      "id": "l4",
      "color": "#b07aa1",
      "name": "to STAGE_PICKUP_IN",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 29.2,
        "y": 124.3
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 450
      }
    },
    {
      "id": "l5",
      "color": "#4e79a7",
      "name": "pick up the staged POLLEN",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 29.2,
        "y": 133.3
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "p4",
      "color": "#9c755f",
      "name": "STAGE_PICKUP_IN park",
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
      "id": "link-3",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 29.2,
        "y": 133.3
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "l6",
      "color": "#f28e2b",
      "name": "back to LEFT_AUTO_HIVE",
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
      "id": "p6",
      "color": "#e15759",
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
        "endDeg": 360
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
      "id": "p7",
      "color": "#59a14f",
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
        "endDeg": 360
      }
    },
    {
      "id": "link-5",
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
      "id": "p8",
      "color": "#edc948",
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
        "endDeg": 360
      }
    },
    {
      "id": "p2",
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
      "id": "link-6",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 29.2,
        "y": 133.3
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "p5",
      "color": "#76b7b2",
      "name": "STAGE_PICKUP_END park",
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
      "lineId": "l2"
    },
    {
      "kind": "path",
      "lineId": "p1"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "l3"
    },
    {
      "kind": "path",
      "lineId": "p3"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "l4"
    },
    {
      "kind": "path",
      "lineId": "l5"
    },
    {
      "kind": "path",
      "lineId": "p4"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "l6"
    },
    {
      "kind": "path",
      "lineId": "p6"
    },
    {
      "kind": "path",
      "lineId": "link-4"
    },
    {
      "kind": "path",
      "lineId": "p7"
    },
    {
      "kind": "path",
      "lineId": "link-5"
    },
    {
      "kind": "path",
      "lineId": "p8"
    },
    {
      "kind": "path",
      "lineId": "p2"
    },
    {
      "kind": "path",
      "lineId": "link-6"
    },
    {
      "kind": "path",
      "lineId": "p5"
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
      "STAGE_START": [
        34.2,
        130.8,
        295
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "LEFT_AUTO_HIVE": [
        61.4,
        120.45,
        268.9
      ],
      "STAGE_PICKUP_IN": [
        29.2,
        124.3,
        90
      ],
      "STAGE_PICKUP_END": [
        29.2,
        133.3,
        90
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ]
    },
    "pathEnds": {
      "l2": "SCORING_FLOWER_STAGING",
      "p1": "LEFT_PARK",
      "p2": "LEFT_PARK",
      "l3": "LEFT_AUTO_HIVE",
      "p3": "LEFT_PARK",
      "l4": "STAGE_PICKUP_IN",
      "p4": "LEFT_PARK",
      "l5": "STAGE_PICKUP_END",
      "p5": "LEFT_PARK",
      "l6": "LEFT_AUTO_HIVE",
      "p6": "LEFT_PARK",
      "p7": "LEFT_PARK",
      "p8": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "c2",
        "kind": "path",
        "lineId": "l2",
        "park": false
      },
      {
        "id": "c3",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 8
      },
      {
        "id": "c4",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c5",
        "kind": "path",
        "lineId": "l3",
        "park": false
      },
      {
        "id": "c6",
        "kind": "firstOf",
        "label": "Wait for TIP 1 (LEFT CELL up)",
        "rows": [
          {
            "when": [
              "CellUp"
            ],
            "cards": [
              {
                "id": "c7",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c8",
                "kind": "path",
                "lineId": "l4",
                "park": false
              },
              {
                "id": "c9",
                "kind": "path",
                "lineId": "l5",
                "park": false
              },
              {
                "id": "c10",
                "kind": "firstOf",
                "label": "Staged POLLEN in?",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1500,
                    "cards": []
                  }
                ]
              },
              {
                "id": "c11",
                "kind": "path",
                "lineId": "l6",
                "park": false
              },
              {
                "id": "c12",
                "kind": "firstOf",
                "label": "LEFT CELL still up?",
                "rows": [
                  {
                    "when": [
                      "CellUp"
                    ],
                    "cards": [
                      {
                        "id": "c13",
                        "kind": "action",
                        "name": "LaunchAll"
                      },
                      {
                        "id": "c14",
                        "kind": "path",
                        "lineId": "p6",
                        "park": true
                      }
                    ]
                  },
                  {
                    "afterMs": 23000,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "c15",
                        "kind": "path",
                        "lineId": "p7",
                        "park": true
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            "afterMs": 14000,
            "matchClock": true,
            "adjustable": true,
            "tip": 1,
            "cards": [
              {
                "id": "c16",
                "kind": "path",
                "lineId": "p8",
                "park": true
              }
            ]
          }
        ]
      }
    ],
    "startAt": "STAGE_START",
    "linkPaths": [
      "link-1",
      "link-2",
      "link-3",
      "link-4",
      "link-5",
      "link-6"
    ],
    "parkFrom": {
      "c2": "p1",
      "c5": "p3",
      "c8": "p3",
      "c9": "p4",
      "c11": "p3"
    },
    "teamSpots": [
      "LEFT_AUTO_HIVE",
      "LEFT_PARK",
      "SCORING_FLOWER_STAGING"
    ],
    "exportName": "L-StagedSweep"
  }
}