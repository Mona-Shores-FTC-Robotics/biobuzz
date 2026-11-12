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
      "id": "to-row",
      "color": "#59a14f",
      "name": "to ROW_APPROACH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 35.4,
        "y": 120
      },
      "controlPoints": [
        {
          "x": 62,
          "y": 30
        },
        {
          "x": 66,
          "y": 80
        },
        {
          "x": 66,
          "y": 118
        },
        {
          "x": 44,
          "y": 116
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "row-park",
      "color": "#f28e2b",
      "name": "ROW_APPROACH park",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 0
      }
    },
    {
      "id": "link-1",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 35.4,
        "y": 129
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "row-to-scoring",
      "color": "#b07aa1",
      "name": "ROW_END to SCORING_FLOWER_STAGING",
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
      "id": "scoring-park",
      "color": "#edc948",
      "name": "SCORING_FLOWER_STAGING park",
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
      "id": "scoring-to-wall",
      "color": "#e15759",
      "name": "SCORING_FLOWER_STAGING to WALL_FLOWER_STAGING",
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
      "controlPoints": [
        {
          "x": 26,
          "y": 108
        },
        {
          "x": 24,
          "y": 70
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 90,
        "endDeg": 180,
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
                "startDeg": 90,
                "endDeg": 180
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 180,
                "endDeg": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "wall-park",
      "color": "#4e79a7",
      "name": "WALL_FLOWER_STAGING park",
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
      "id": "to-lane-right",
      "color": "#9c755f",
      "name": "to LANE_RIGHT",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 26,
        "y": 40
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "lane-right-park",
      "color": "#76b7b2",
      "name": "LANE_RIGHT park",
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
          "x": 24,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 0
      }
    },
    {
      "id": "link-4",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 26,
        "y": 40
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-garden",
      "color": "#ff9da7",
      "name": "to GARDEN_ROW_START",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 22,
        "y": 8.0
      },
      "controlPoints": [
        {
          "x": 32,
          "y": 20
        },
        {
          "x": 36,
          "y": 8.0
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "garden-row-park",
      "color": "#59a14f",
      "name": "GARDEN_ROW_START park",
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
          "x": 24,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 0
      }
    },
    {
      "id": "link-5",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 12,
        "y": 8.0
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-corner",
      "color": "#f28e2b",
      "name": "to RIGHT_LAUNCH_CORNER",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30.2,
        "y": 10.7
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "corner-park",
      "color": "#b07aa1",
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
          "y": 55
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 180,
        "endDeg": 0,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.4,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 180,
                "endDeg": 180
              }
            },
            {
              "startProgress": 0.4,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 180,
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
      "lineId": "to-row"
    },
    {
      "kind": "path",
      "lineId": "row-park"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "row-to-scoring"
    },
    {
      "kind": "path",
      "lineId": "scoring-park"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "scoring-to-wall"
    },
    {
      "kind": "path",
      "lineId": "wall-park"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "to-lane-right"
    },
    {
      "kind": "path",
      "lineId": "lane-right-park"
    },
    {
      "kind": "path",
      "lineId": "link-4"
    },
    {
      "kind": "path",
      "lineId": "to-garden"
    },
    {
      "kind": "path",
      "lineId": "garden-row-park"
    },
    {
      "kind": "path",
      "lineId": "link-5"
    },
    {
      "kind": "path",
      "lineId": "to-corner"
    },
    {
      "kind": "path",
      "lineId": "corner-park"
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
    "maxVelocity": 70,
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
        "SpinUp",
        "SweepRow",
        "StreamOn",
        "StreamOff"
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
        "SpinUp": 0.1,
        "SweepRow": 2.5,
        "StreamOn": 0.1,
        "StreamOff": 0.1
      }
    },
    "points": {
      "RIGHT_START": [
        55.6,
        7.7,
        90
      ],
      "ROW_APPROACH": [
        35.4,
        120,
        90
      ],
      "ROW_END": [
        35.4,
        129,
        90
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "WALL_FLOWER_STAGING": [
        23.1,
        47.2,
        180
      ],
      "LANE_RIGHT": [
        26,
        40,
        180
      ],
      "GARDEN_ROW_START": [
        22,
        8.0,
        180
      ],
      "GARDEN_ROW_END": [
        12,
        8.0,
        180
      ],
      "RIGHT_LAUNCH_CORNER": [
        30.2,
        10.7,
        180
      ],
      "RIGHT_PARK": [
        14.5,
        95.7,
        0
      ]
    },
    "pathEnds": {
      "to-row": "ROW_APPROACH",
      "row-park": "RIGHT_PARK",
      "row-to-scoring": "SCORING_FLOWER_STAGING",
      "scoring-park": "RIGHT_PARK",
      "scoring-to-wall": "WALL_FLOWER_STAGING",
      "wall-park": "RIGHT_PARK",
      "to-lane-right": "LANE_RIGHT",
      "lane-right-park": "RIGHT_PARK",
      "to-garden": "GARDEN_ROW_START",
      "garden-row-park": "RIGHT_PARK",
      "to-corner": "RIGHT_LAUNCH_CORNER",
      "corner-park": "RIGHT_PARK"
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
        "lineId": "to-row",
        "park": false
      },
      {
        "id": "c-a-4",
        "kind": "action",
        "name": "SweepRow",
        "timeoutS": 4
      },
      {
        "id": "c-w-5",
        "kind": "firstOf",
        "label": "Wait for the LEFT CELL",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": []
          }
        ]
      },
      {
        "id": "c-a-6",
        "kind": "action",
        "name": "StreamOn"
      },
      {
        "id": "c-p-7",
        "kind": "path",
        "lineId": "row-to-scoring",
        "park": false
      },
      {
        "id": "c-a-8",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 4.5
      },
      {
        "id": "c-w-9",
        "kind": "firstOf",
        "label": "Wait until empty",
        "rows": [
          {
            "when": [
              "Empty"
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
        "id": "c-a-10",
        "kind": "action",
        "name": "StreamOff"
      },
      {
        "id": "c-p-11",
        "kind": "path",
        "lineId": "scoring-to-wall",
        "park": false
      },
      {
        "id": "c-a-12",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 4.5
      },
      {
        "id": "c-p-13",
        "kind": "path",
        "lineId": "to-lane-right",
        "park": false
      },
      {
        "id": "c-w-14",
        "kind": "firstOf",
        "label": "Wait for the RIGHT CELL",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ]
      },
      {
        "id": "c-a-15",
        "kind": "action",
        "name": "StreamOn"
      },
      {
        "id": "c-w-16",
        "kind": "firstOf",
        "label": "Wait until empty",
        "rows": [
          {
            "when": [
              "Empty"
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
        "id": "c-p-17",
        "kind": "path",
        "lineId": "to-garden",
        "park": false
      },
      {
        "id": "c-a-18",
        "kind": "action",
        "name": "SweepRow",
        "timeoutS": 4
      },
      {
        "id": "c-p-19",
        "kind": "path",
        "lineId": "to-corner",
        "park": false
      },
      {
        "id": "c-w-20",
        "kind": "firstOf",
        "label": "Wait until the last piece is out",
        "rows": [
          {
            "when": [
              "Empty"
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
        "id": "c-p-21",
        "kind": "path",
        "lineId": "corner-park",
        "park": true
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
      "c-p-3": "row-park",
      "c-p-7": "scoring-park",
      "c-p-11": "wall-park",
      "c-p-13": "lane-right-park",
      "c-p-17": "garden-row-park"
    },
    "teamSpots": [
      "RIGHT_PARK",
      "RIGHT_START",
      "SCORING_FLOWER_STAGING",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "R-Three-Tip-Stream"
  }
}
