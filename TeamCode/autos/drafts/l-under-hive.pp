{
  "version": "2.0.0",
  "timestamp": "2026-11-11T00:00:00.000Z",
  "startPoint": {
    "x": 62.3,
    "y": 133.8,
    "headingDeg": 270,
    "locked": false
  },
  "lines": [
    {
      "kind": "atomic",
      "id": "l-open",
      "endPoint": {
        "x": 59.9,
        "y": 116.2
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      },
      "locked": false,
      "name": "open to LEFT_CLOSE_HIVE",
      "color": "#4e79a7",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "compound",
      "id": "l-early-garden",
      "segments": [
        {
          "kind": "atomic",
          "id": "l-empty-under-hive",
          "endPoint": {
            "x": 56.1,
            "y": 26
          },
          "controlPoints": [
            {
              "x": 58.76832619047272,
              "y": 101.56857289931824
            },
            {
              "x": 65.20046354104544,
              "y": 71.90515781237575
            },
            {
              "x": 60.6,
              "y": 40.9
            }
          ],
          "heading": {
            "type": "piecewise",
            "piecewiseHeading": {
              "segments": [
                {
                  "startProgress": 0,
                  "endProgress": 0.1,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 270,
                    "endDeg": 270
                  }
                },
                {
                  "startProgress": 0.1,
                  "endProgress": 1,
                  "interpolationType": "constant",
                  "parameters": {
                    "degrees": 270
                  }
                }
              ]
            }
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "l-empty-to-garden",
          "endPoint": {
            "x": 11.5,
            "y": 22.1
          },
          "controlPoints": [],
          "heading": {
            "type": "constant",
            "degrees": 270
          },
          "locked": false
        }
      ],
      "name": "early garden to GARDEN_STAGING",
      "color": "#4e79a7",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "l-garden-exit",
      "endPoint": {
        "x": 56.1,
        "y": 25.3
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 360
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 90
              }
            }
          ]
        }
      },
      "locked": false,
      "name": "garden exit to RIGHT_CLOSE_HIVE",
      "color": "#4e79a7",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "compound",
      "id": "l-under-hive-park",
      "segments": [
        {
          "kind": "atomic",
          "id": "l-north-under-hive",
          "endPoint": {
            "x": 59,
            "y": 104
          },
          "controlPoints": [
            {
              "x": 60.6,
              "y": 40.9
            },
            {
              "x": 65.2,
              "y": 71.9
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 90,
            "endDeg": 90
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "l-park-turn",
          "endPoint": {
            "x": 14.5,
            "y": 117.4
          },
          "controlPoints": [
            {
              "x": 59,
              "y": 120
            },
            {
              "x": 35,
              "y": 118.5
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
                    "startDeg": 90,
                    "endDeg": 0
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
          },
          "locked": false
        }
      ],
      "name": "under hive park to LEFT_PARK",
      "color": "#4e79a7",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "l-link-left",
      "endPoint": {
        "x": 59.9,
        "y": 116.2
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 270
      },
      "locked": false,
      "name": "Link to LEFT_CLOSE_HIVE"
    },
    {
      "kind": "atomic",
      "id": "l-left-abort-park",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 0
      },
      "locked": false,
      "name": "left abort park to LEFT_PARK",
      "color": "#4e79a7",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "l-link-garden",
      "endPoint": {
        "x": 11.5,
        "y": 22.1
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 270
      },
      "locked": false,
      "name": "Link to GARDEN_STAGING"
    },
    {
      "kind": "compound",
      "id": "l-garden-abort-park",
      "segments": [
        {
          "kind": "atomic",
          "id": "l-garden-back-east",
          "endPoint": {
            "x": 56.1,
            "y": 26
          },
          "controlPoints": [],
          "heading": {
            "type": "piecewise",
            "piecewiseHeading": {
              "segments": [
                {
                  "startProgress": 0,
                  "endProgress": 0.5,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 270,
                    "endDeg": 360
                  }
                },
                {
                  "startProgress": 0.5,
                  "endProgress": 1,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 0,
                    "endDeg": 90
                  }
                }
              ]
            }
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "l-north-under-hive-abort",
          "endPoint": {
            "x": 59,
            "y": 104
          },
          "controlPoints": [
            {
              "x": 60.6,
              "y": 40.9
            },
            {
              "x": 65.2,
              "y": 71.9
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 90,
            "endDeg": 90
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "l-park-turn-abort",
          "endPoint": {
            "x": 14.5,
            "y": 117.4
          },
          "controlPoints": [
            {
              "x": 59,
              "y": 120
            },
            {
              "x": 35,
              "y": 118.5
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
                    "startDeg": 90,
                    "endDeg": 0
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
          },
          "locked": false
        }
      ],
      "name": "garden abort park to LEFT_PARK",
      "color": "#4e79a7",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
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
  "sequence": [],
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
        "LaunchOne",
        "SpinUp",
        "PickFlower",
        "GardenGather"
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
        "LaunchOne": 0.5,
        "SpinUp": 0.1,
        "PickFlower": 4.0,
        "GardenGather": 5.0
      }
    },
    "points": {
      "LEFT_START": [
        62.3,
        133.8,
        270
      ],
      "LEFT_CLOSE_HIVE": [
        59.9,
        116.2,
        270
      ],
      "GARDEN_STAGING": [
        11.5,
        22.1,
        270
      ],
      "RIGHT_CLOSE_HIVE": [
        56.1,
        25.3,
        90
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ]
    },
    "pathEnds": {
      "l-open": "LEFT_CLOSE_HIVE",
      "l-early-garden": "GARDEN_STAGING",
      "l-garden-exit": "RIGHT_CLOSE_HIVE",
      "l-under-hive-park": "LEFT_PARK",
      "l-left-abort-park": "LEFT_PARK",
      "l-garden-abort-park": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "L-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "card-l-open",
        "kind": "path",
        "lineId": "l-open",
        "park": false
      },
      {
        "id": "L-wait-left",
        "kind": "firstOf",
        "label": "LEFT up before 9 s, otherwise park",
        "rows": [
          {
            "afterMs": 9000,
            "matchClock": true,
            "cards": [
              {
                "id": "card-l-left-abort-park",
                "kind": "path",
                "lineId": "l-left-abort-park",
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
                "id": "L-launch-preloads",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "card-l-early-garden",
                "kind": "path",
                "lineId": "l-early-garden",
                "park": false
              },
              {
                "id": "L-garden",
                "kind": "action",
                "name": "GardenGather",
                "timeoutS": 5.5
              },
              {
                "id": "card-l-garden-exit",
                "kind": "path",
                "lineId": "l-garden-exit",
                "park": false
              },
              {
                "id": "L-wait-right",
                "kind": "firstOf",
                "label": "RIGHT up before 22 s, otherwise park",
                "rows": [
                  {
                    "afterMs": 22000,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "card-l-under-hive-park-timeout",
                        "kind": "path",
                        "lineId": "l-under-hive-park",
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
                        "id": "L-launch-right",
                        "kind": "action",
                        "name": "LaunchAll",
                        "timeoutS": 1
                      },
                      {
                        "id": "card-l-under-hive-park",
                        "kind": "path",
                        "lineId": "l-under-hive-park",
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
    "exportName": "L-UnderHive",
    "parkFrom": {
      "card-l-open": "l-left-abort-park",
      "card-l-early-garden": "l-garden-abort-park",
      "card-l-garden-exit": "l-under-hive-park"
    }
  }
}
