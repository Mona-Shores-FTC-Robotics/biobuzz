{
  "version": "2.0.0",
  "timestamp": "2026-11-25T14:41:00.441Z",
  "startPoint": {
    "x": 62.3,
    "y": 133.8,
    "locked": false,
    "headingDeg": 270
  },
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
  "lines": [
    {
      "id": "lr-to-offset",
      "color": "#59a14f",
      "name": "LEFT_START straight to LEFT_AUTO_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [
        {
          "x": 56,
          "y": 124
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 298.7
      }
    },
    {
      "id": "lr-offset-park",
      "color": "#9c755f",
      "name": "LEFT_AUTO_OFFSET to LEFT_PARK (guard)",
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
          "y": 121
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 0
      }
    },
    {
      "id": "lr-link-1",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 298.7
      }
    },
    {
      "id": "lr-offset-park-2",
      "color": "#9c755f",
      "name": "LEFT never up: LEFT_AUTO_OFFSET to LEFT_PARK",
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
          "y": 121
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 0
      }
    },
    {
      "id": "lr-link-2",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 298.7
      }
    },
    {
      "id": "lr-to-wf",
      "color": "#59a14f",
      "name": "LEFT_AUTO_OFFSET down the outer lane to WALL_FLOWER_STAGING",
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
          "x": 32,
          "y": 105
        },
        {
          "x": 30,
          "y": 80
        },
        {
          "x": 27,
          "y": 62
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 180
      }
    },
    {
      "id": "lr-wf-park",
      "color": "#9c755f",
      "name": "WALL_FLOWER_STAGING to LEFT_PARK (guard)",
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
          "y": 75
        },
        {
          "x": 30,
          "y": 105
        },
        {
          "x": 22,
          "y": 115
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 0
      }
    },
    {
      "id": "lr-link-3",
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
      "id": "lr-to-roff",
      "color": "#59a14f",
      "name": "WALL_FLOWER_STAGING to RIGHT_AUTO_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 32.1,
        "y": 16.4
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 30
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 61
      }
    },
    {
      "id": "lr-park",
      "color": "#9c755f",
      "name": "RIGHT_AUTO_OFFSET up the outer lane to LEFT_PARK",
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
          "y": 40
        },
        {
          "x": 28,
          "y": 80
        },
        {
          "x": 28,
          "y": 105
        },
        {
          "x": 20,
          "y": 115
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 61,
        "endDeg": 0
      }
    },
    {
      "id": "lr-link-4",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 32.1,
        "y": 16.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 61
      }
    },
    {
      "id": "lr-park-2",
      "color": "#9c755f",
      "name": "RIGHT not up: RIGHT_AUTO_OFFSET to LEFT_PARK",
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
          "y": 40
        },
        {
          "x": 28,
          "y": 80
        },
        {
          "x": 28,
          "y": 105
        },
        {
          "x": 20,
          "y": 115
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 61,
        "endDeg": 0
      }
    }
  ],
  "sequence": [
    {
      "kind": "path",
      "lineId": "lr-to-offset"
    },
    {
      "kind": "path",
      "lineId": "lr-offset-park"
    },
    {
      "kind": "path",
      "lineId": "lr-link-1"
    },
    {
      "kind": "path",
      "lineId": "lr-offset-park-2"
    },
    {
      "kind": "path",
      "lineId": "lr-link-2"
    },
    {
      "kind": "path",
      "lineId": "lr-to-wf"
    },
    {
      "kind": "path",
      "lineId": "lr-wf-park"
    },
    {
      "kind": "path",
      "lineId": "lr-link-3"
    },
    {
      "kind": "path",
      "lineId": "lr-to-roff"
    },
    {
      "kind": "path",
      "lineId": "lr-park"
    },
    {
      "kind": "path",
      "lineId": "lr-link-4"
    },
    {
      "kind": "path",
      "lineId": "lr-park-2"
    }
  ],
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
      "LEFT_START": [
        62.3,
        133.8,
        270
      ],
      "LEFT_AUTO_OFFSET": [
        40.7,
        121.1,
        298.7
      ],
      "WALL_FLOWER_STAGING": [
        23.1,
        47.2,
        180
      ],
      "RIGHT_AUTO_OFFSET": [
        32.1,
        16.4,
        61.0
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ]
    },
    "pathEnds": {
      "lr-to-offset": "LEFT_AUTO_OFFSET",
      "lr-offset-park": "LEFT_PARK",
      "lr-offset-park-2": "LEFT_PARK",
      "lr-to-wf": "WALL_FLOWER_STAGING",
      "lr-wf-park": "LEFT_PARK",
      "lr-to-roff": "RIGHT_AUTO_OFFSET",
      "lr-park": "LEFT_PARK",
      "lr-park-2": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "c-lr-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-lr-to-offset",
        "kind": "path",
        "lineId": "lr-to-offset",
        "park": false
      },
      {
        "id": "c-lr-left-up",
        "kind": "firstOf",
        "label": "LEFT up? (TIP 1 came)",
        "rows": [
          {
            "when": [
              "CellUp"
            ],
            "cards": [
              {
                "id": "c-lr-fire1",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-lr-to-wf",
                "kind": "path",
                "lineId": "lr-to-wf",
                "park": false
              },
              {
                "id": "c-lr-pick",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 8
              },
              {
                "id": "c-lr-to-roff",
                "kind": "path",
                "lineId": "lr-to-roff",
                "park": false
              },
              {
                "id": "c-lr-right-up",
                "kind": "firstOf",
                "label": "RIGHT up? (TIP 2 came)",
                "rows": [
                  {
                    "when": [
                      "CellUp"
                    ],
                    "cards": [
                      {
                        "id": "c-lr-fire2",
                        "kind": "action",
                        "name": "LaunchAll"
                      },
                      {
                        "id": "c-lr-park",
                        "kind": "path",
                        "lineId": "lr-park",
                        "park": true
                      }
                    ]
                  },
                  {
                    "afterMs": 25000,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "c-lr-park-2",
                        "kind": "path",
                        "lineId": "lr-park-2",
                        "park": true
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            "afterMs": 12000,
            "matchClock": true,
            "cards": [
              {
                "id": "c-lr-park-0",
                "kind": "path",
                "lineId": "lr-offset-park-2",
                "park": true
              }
            ]
          }
        ]
      }
    ],
    "parkFrom": {
      "c-lr-to-offset": "lr-offset-park",
      "c-lr-to-wf": "lr-wf-park",
      "c-lr-to-roff": "lr-park"
    },
    "startAt": "LEFT_START",
    "linkPaths": [
      "lr-link-1",
      "lr-link-2",
      "lr-link-3",
      "lr-link-4"
    ],
    "teamSpots": [
      "LEFT_AUTO_OFFSET",
      "LEFT_PARK",
      "LEFT_START",
      "RIGHT_AUTO_OFFSET",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "L-RightUnderHive"
  }
}
