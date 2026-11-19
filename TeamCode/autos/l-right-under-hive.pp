{
  "version": "2.0.0",
  "timestamp": "2026-11-19T20:18:50.247Z",
  "startPoint": {
    "x": 62.3,
    "y": 133.8,
    "locked": false,
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "l-ru-open",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 268.9
      }
    },
    {
      "id": "l-ru-hive-park",
      "color": "#4e79a7",
      "name": "LEFT_AUTO_HIVE, outer lane to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 50,
          "y": 112
        },
        {
          "x": 36,
          "y": 102
        },
        {
          "x": 28,
          "y": 95
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 268.9,
                "endDeg": 360
              }
            },
            {
              "startProgress": 0.6,
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
      "id": "l-ru-sf",
      "color": "#4e79a7",
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
      "id": "l-ru-sf-park",
      "color": "#4e79a7",
      "name": "SCORING FLOWER to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 38,
          "y": 112.6
        },
        {
          "x": 31.8,
          "y": 94.1
        }
      ],
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
      "id": "l-ru-sf-offset",
      "color": "#4e79a7",
      "name": "SCORING FLOWER to LEFT_AUTO_OFFSET",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 298.7
      }
    },
    {
      "id": "l-ru-offset-park-2",
      "color": "#4e79a7",
      "name": "late: LEFT_AUTO_OFFSET to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 38,
          "y": 104
        },
        {
          "x": 34,
          "y": 95
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 360
      }
    },
    {
      "id": "link-3",
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
        "degrees": -61.299999999999955
      }
    },
    {
      "id": "l-ru-lane",
      "color": "#4e79a7",
      "name": "outer lane to WALL_FLOWER_STAGING",
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
          "y": 110
        },
        {
          "x": 30,
          "y": 96
        },
        {
          "x": 26,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 180
      }
    },
    {
      "id": "l-ru-offset-park",
      "color": "#4e79a7",
      "name": "LEFT_AUTO_OFFSET to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 38,
          "y": 104
        },
        {
          "x": 34,
          "y": 95
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 360
      }
    },
    {
      "id": "link-4",
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
        "degrees": 179.99999999999994
      }
    },
    {
      "id": "l-ru-wall-park",
      "color": "#4e79a7",
      "name": "WALL_FLOWER_STAGING to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 75
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
      "id": "l-ru-lane-late",
      "color": "#4e79a7",
      "name": "no TIP 1, outer lane to WALL_FLOWER_STAGING",
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
          "x": 50,
          "y": 112
        },
        {
          "x": 34,
          "y": 104
        },
        {
          "x": 28,
          "y": 96
        },
        {
          "x": 26,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 180
      }
    },
    {
      "id": "l-ru-wall-park-late",
      "color": "#4e79a7",
      "name": "no TIP 1, WALL_FLOWER_STAGING to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 75
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
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
      "lineId": "l-ru-open"
    },
    {
      "kind": "path",
      "lineId": "l-ru-hive-park"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "l-ru-sf"
    },
    {
      "kind": "path",
      "lineId": "l-ru-sf-park"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "l-ru-sf-offset"
    },
    {
      "kind": "path",
      "lineId": "l-ru-offset-park-2"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "l-ru-lane"
    },
    {
      "kind": "path",
      "lineId": "l-ru-offset-park"
    },
    {
      "kind": "path",
      "lineId": "link-4"
    },
    {
      "kind": "path",
      "lineId": "l-ru-wall-park"
    },
    {
      "kind": "path",
      "lineId": "link-5"
    },
    {
      "kind": "path",
      "lineId": "l-ru-lane-late"
    },
    {
      "kind": "path",
      "lineId": "l-ru-wall-park-late"
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
      "LEFT_AUTO_HIVE": [
        61.4,
        120.45,
        268.9
      ],
      "LEFT_AUTO_OFFSET": [
        40.7,
        121.1,
        298.7
      ],
      "LEFT_START": [
        62.3,
        133.8,
        270
      ],
      "RIGHT_PARK": [
        14.5,
        98.7,
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
      ]
    },
    "pathEnds": {
      "l-ru-open": "LEFT_AUTO_HIVE",
      "l-ru-sf": "SCORING_FLOWER_STAGING",
      "l-ru-sf-offset": "LEFT_AUTO_OFFSET",
      "l-ru-lane": "WALL_FLOWER_STAGING",
      "l-ru-lane-late": "WALL_FLOWER_STAGING",
      "l-ru-wall-park": "RIGHT_PARK",
      "l-ru-wall-park-late": "RIGHT_PARK",
      "l-ru-hive-park": "RIGHT_PARK",
      "l-ru-offset-park": "RIGHT_PARK",
      "l-ru-offset-park-2": "RIGHT_PARK",
      "l-ru-sf-park": "RIGHT_PARK"
    },
    "cards": [
      {
        "id": "c-l-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-l-open",
        "kind": "path",
        "lineId": "l-ru-open",
        "park": false
      },
      {
        "id": "c-l-wait",
        "kind": "firstOf",
        "label": "Wait for TIP 1 until 20 s",
        "rows": [
          {
            "when": [
              "CellUp"
            ],
            "cards": [
              {
                "id": "c-l-pre",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-l-sf",
                "kind": "path",
                "lineId": "l-ru-sf",
                "park": false
              },
              {
                "id": "c-l-pick",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 8
              },
              {
                "id": "c-l-sf-offset",
                "kind": "path",
                "lineId": "l-ru-sf-offset",
                "park": false
              },
              {
                "id": "c-l-wall-gate",
                "kind": "firstOf",
                "label": "WALL FLOWER only before 22 s, otherwise park",
                "rows": [
                  {
                    "afterMs": 22000,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "c-l-check-late",
                        "kind": "firstOf",
                        "label": "LEFT still up?",
                        "rows": [
                          {
                            "when": [
                              "CellUp"
                            ],
                            "cards": [
                              {
                                "id": "c-l-second-late",
                                "kind": "action",
                                "name": "LaunchAll"
                              }
                            ]
                          },
                          {
                            "afterMs": 750,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "c-l-gate-park",
                        "kind": "path",
                        "lineId": "l-ru-offset-park-2",
                        "park": true
                      }
                    ]
                  },
                  {
                    "when": [
                      "Always"
                    ],
                    "cards": [
                      {
                        "id": "c-l-check",
                        "kind": "firstOf",
                        "label": "LEFT still up?",
                        "rows": [
                          {
                            "when": [
                              "CellUp"
                            ],
                            "cards": [
                              {
                                "id": "c-l-second",
                                "kind": "action",
                                "name": "LaunchAll"
                              }
                            ]
                          },
                          {
                            "afterMs": 750,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "c-l-lane",
                        "kind": "path",
                        "lineId": "l-ru-lane",
                        "park": false
                      },
                      {
                        "id": "c-l-wall",
                        "kind": "action",
                        "name": "PickFlower",
                        "timeoutS": 8
                      },
                      {
                        "id": "c-l-park",
                        "kind": "path",
                        "lineId": "l-ru-wall-park",
                        "park": true
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            "afterMs": 20000,
            "matchClock": true,
            "adjustable": true,
            "tip": 1,
            "cards": [
              {
                "id": "c-l-lane-late",
                "kind": "path",
                "lineId": "l-ru-lane-late",
                "park": false
              },
              {
                "id": "c-l-wall-late",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 8
              },
              {
                "id": "c-l-park-late",
                "kind": "path",
                "lineId": "l-ru-wall-park-late",
                "park": true
              }
            ]
          }
        ],
        "alongside": "SpinUp"
      }
    ],
    "startAt": "LEFT_START",
    "linkPaths": [
      "link-1",
      "link-2",
      "link-3",
      "link-4",
      "link-5"
    ],
    "parkFrom": {
      "c-l-open": "l-ru-hive-park",
      "c-l-sf": "l-ru-sf-park",
      "c-l-sf-offset": "l-ru-sf-park",
      "c-l-lane": "l-ru-offset-park",
      "c-l-lane-late": "l-ru-hive-park"
    },
    "teamSpots": [
      "LEFT_AUTO_HIVE",
      "LEFT_AUTO_OFFSET",
      "LEFT_START",
      "RIGHT_PARK",
      "SCORING_FLOWER_STAGING",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "L-RightUnderHive"
  }
}
