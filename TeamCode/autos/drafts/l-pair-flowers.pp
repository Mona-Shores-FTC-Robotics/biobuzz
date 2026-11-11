{
  "version": "2.0.0",
  "timestamp": "2026-11-11T05:00:00.000Z",
  "startPoint": {
    "x": 62.3,
    "y": 133.8,
    "locked": false,
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-offset",
      "color": "#59a14f",
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
      "controlPoints": [
        {
          "x": 58,
          "y": 122
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 296.4
      }
    },
    {
      "id": "offset-park",
      "color": "#f28e2b",
      "name": "LEFT_LAUNCH_OFFSET park",
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
        "startDeg": 296.4,
        "endDeg": 0
      }
    },
    {
      "id": "link-1",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 121.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 296.4
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
        "startDeg": 296.4,
        "endDeg": 90
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
      "id": "scoring-back",
      "color": "#e15759",
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
      "id": "offset-to-wall",
      "color": "#4e79a7",
      "name": "wall lane to WALL_FLOWER_STAGING",
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
          "x": 24,
          "y": 104
        },
        {
          "x": 24,
          "y": 70
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 296.4,
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
                "startDeg": 296.4,
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
      "color": "#9c755f",
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
      "controlPoints": [
        {
          "x": 26,
          "y": 80
        }
      ],
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
      "color": "#76b7b2",
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
        "type": "linear",
        "startDeg": 180,
        "endDeg": 27.7
      }
    },
    {
      "id": "lane-park",
      "color": "#ff9da7",
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
      "controlPoints": [
        {
          "x": 27,
          "y": 70
        },
        {
          "x": 27,
          "y": 105
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 27.7,
        "endDeg": 0,
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
                "startDeg": 27.7,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.3,
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
      "lineId": "to-offset"
    },
    {
      "kind": "path",
      "lineId": "offset-park"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "to-scoring"
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
      "lineId": "scoring-back"
    },
    {
      "kind": "path",
      "lineId": "offset-to-wall"
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
      "lineId": "lane-park"
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
      "LEFT_START": [
        62.3,
        133.8,
        270
      ],
      "LEFT_LAUNCH_OFFSET": [
        41,
        121.5,
        296.4
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
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
        27.7
      ]
    },
    "pathEnds": {
      "to-offset": "LEFT_LAUNCH_OFFSET",
      "offset-park": "LEFT_PARK",
      "to-scoring": "SCORING_FLOWER_STAGING",
      "scoring-park": "LEFT_PARK",
      "scoring-back": "LEFT_LAUNCH_OFFSET",
      "offset-to-wall": "WALL_FLOWER_STAGING",
      "wall-park": "LEFT_PARK",
      "to-lane-right": "LANE_RIGHT",
      "lane-park": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "c-a-1",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-p-2",
        "kind": "path",
        "lineId": "to-offset",
        "park": false
      },
      {
        "id": "c-w-16",
        "kind": "firstOf",
        "label": "LEFT up before 13 s, otherwise park",
        "rows": [
          {
            "afterMs": 13000,
            "matchClock": true,
            "cards": [
              {
                "id": "c-p-3",
                "kind": "path",
                "lineId": "offset-park",
                "park": true
              }
            ]
          },
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [
              {
                "id": "c-a-4",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-p-5",
                "kind": "path",
                "lineId": "to-scoring",
                "park": false
              },
              {
                "id": "c-a-6",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 4.5
              },
              {
                "id": "c-p-7",
                "kind": "path",
                "lineId": "scoring-back",
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
                "lineId": "offset-to-wall",
                "park": false
              },
              {
                "id": "c-a-10",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 4.5
              },
              {
                "id": "c-p-11",
                "kind": "path",
                "lineId": "to-lane-right",
                "park": false
              },
              {
                "id": "c-w-15",
                "kind": "firstOf",
                "label": "RIGHT up before 23.5 s, otherwise park",
                "rows": [
                  {
                    "afterMs": 23500,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "c-p-12",
                        "kind": "path",
                        "lineId": "lane-park",
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
                        "id": "c-a-13",
                        "kind": "action",
                        "name": "LaunchAll"
                      },
                      {
                        "id": "c-p-14",
                        "kind": "path",
                        "lineId": "lane-park",
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
      "link-3"
    ],
    "parkFrom": {
      "c-p-2": "offset-park",
      "c-p-5": "scoring-park",
      "c-p-9": "wall-park"
    },
    "teamSpots": [
      "LEFT_LAUNCH_OFFSET",
      "LEFT_PARK",
      "LEFT_START",
      "SCORING_FLOWER_STAGING",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "L-Pair-Flowers"
  }
}
