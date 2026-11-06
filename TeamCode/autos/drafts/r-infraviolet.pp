{
  "version": "2.0.0",
  "timestamp": "2026-11-06T17:44:57.818Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "headingDeg": 90,
    "locked": false
  },
  "lines": [
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-wall-flower",
      "name": "wall flower to WALL_FLOWER_STAGING",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 180
      },
      "color": "#e15759"
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-wall-lane-north",
      "name": "wall lane north to LEFT_LAUNCH_CLOSE",
      "endPoint": {
        "x": 59.9,
        "y": 117.5
      },
      "controlPoints": [
        {
          "x": 29.298561851629266,
          "y": 127.30521572102954
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.12,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.12,
              "endProgress": 0.56,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.56,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 270
              }
            }
          ]
        }
      },
      "color": "#e15759"
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-scoring-flower",
      "name": "scoring flower to SCORING_FLOWER_STAGING",
      "endPoint": {
        "x": 47.2,
        "y": 119.3
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 90
      },
      "color": "#e15759"
    },
    {
      "kind": "compound",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-loaded-under-hive",
      "name": "loaded under hive to RIGHT_PAIR_CLOSE",
      "color": "#e15759",
      "segments": [
        {
          "kind": "atomic",
          "id": "r-flower-forward-turn",
          "endPoint": {
            "x": 59.9,
            "y": 117.5
          },
          "controlPoints": [
            {
              "x": 47.2,
              "y": 132
            },
            {
              "x": 59.9,
              "y": 132
            }
          ],
          "heading": {
            "type": "tangential",
            "reverse": false
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "r-south-under-hive",
          "endPoint": {
            "x": 58.3,
            "y": 41
          },
          "controlPoints": [
            {
              "x": 58.76832619047272,
              "y": 101.56857289931824
            },
            {
              "x": 65.20046354104544,
              "y": 71.90515781237575
            }
          ],
          "heading": {
            "type": "constant",
            "degrees": 270
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "r-right-aim-turn",
          "endPoint": {
            "x": 58.3,
            "y": 35
          },
          "controlPoints": [],
          "heading": {
            "type": "linear",
            "startDeg": 270,
            "endDeg": 97.23932587081833
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "r-right-close-approach",
          "endPoint": {
            "x": 59.5,
            "y": 31
          },
          "controlPoints": [],
          "heading": {
            "type": "constant",
            "degrees": 97.23932587081833
          },
          "locked": false
        }
      ]
    },
    {
      "kind": "compound",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-near-park",
      "name": "near park to RIGHT_PARK",
      "color": "#e15759",
      "segments": [
        {
          "kind": "atomic",
          "id": "r-clear-right-shot",
          "endPoint": {
            "x": 54,
            "y": 40
          },
          "controlPoints": [
            {
              "x": 59,
              "y": 37
            },
            {
              "x": 57,
              "y": 40
            }
          ],
          "heading": {
            "type": "piecewise",
            "piecewiseHeading": {
              "segments": [
                {
                  "startProgress": 0,
                  "endProgress": 0.45,
                  "interpolationType": "constant",
                  "parameters": {
                    "degrees": 97.23932587081833
                  }
                },
                {
                  "startProgress": 0.45,
                  "endProgress": 1,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 97.23932587081833,
                    "endDeg": 180
                  }
                }
              ]
            }
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "r-pass-above-offset",
          "endPoint": {
            "x": 33,
            "y": 40
          },
          "controlPoints": [],
          "heading": {
            "type": "constant",
            "degrees": 180
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "r-near-park-arrival",
          "endPoint": {
            "x": 14.5,
            "y": 95.7
          },
          "controlPoints": [
            {
              "x": 30,
              "y": 65
            },
            {
              "x": 30,
              "y": 87
            }
          ],
          "heading": {
            "type": "piecewise",
            "piecewiseHeading": {
              "segments": [
                {
                  "startProgress": 0,
                  "endProgress": 0.2,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 180,
                    "endDeg": 90
                  }
                },
                {
                  "startProgress": 0.2,
                  "endProgress": 0.65,
                  "interpolationType": "constant",
                  "parameters": {
                    "degrees": 90
                  }
                },
                {
                  "startProgress": 0.65,
                  "endProgress": 1,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 90,
                    "endDeg": 0
                  }
                }
              ]
            }
          },
          "locked": false
        }
      ]
    },
    {
      "kind": "atomic",
      "id": "r-link-left-abort-park",
      "name": "Link to LEFT_LAUNCH_CLOSE",
      "endPoint": {
        "x": 59.9,
        "y": 117.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 270
      },
      "locked": false
    },
    {
      "kind": "compound",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-left-abort-park",
      "name": "left abort park to RIGHT_PARK",
      "color": "#e15759",
      "segments": [
        {
          "kind": "atomic",
          "id": "r-abort-clear-hive",
          "endPoint": {
            "x": 32,
            "y": 108
          },
          "controlPoints": [
            {
              "x": 44,
              "y": 112
            }
          ],
          "heading": {
            "type": "constant",
            "degrees": 270
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "r-abort-clear-far-park",
          "endPoint": {
            "x": 32,
            "y": 96
          },
          "controlPoints": [],
          "heading": {
            "type": "constant",
            "degrees": 270
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "r-abort-near-park",
          "endPoint": {
            "x": 14.5,
            "y": 95.7
          },
          "controlPoints": [],
          "heading": {
            "type": "linear",
            "startDeg": 270,
            "endDeg": 0
          },
          "locked": false
        }
      ]
    },
    {
      "kind": "atomic",
      "id": "r-link-flower-abort-park",
      "name": "Link to SCORING_FLOWER_STAGING",
      "endPoint": {
        "x": 47.2,
        "y": 119.3
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 90
      },
      "locked": false
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-flower-abort-park",
      "name": "flower abort park to RIGHT_PARK",
      "endPoint": {
        "x": 14.5,
        "y": 95.7
      },
      "controlPoints": [
        {
          "x": 40,
          "y": 88
        },
        {
          "x": 28,
          "y": 89
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 0
      },
      "color": "#e15759"
    },
    {
      "kind": "atomic",
      "id": "r-link-wall-abort-park",
      "name": "Link to WALL_FLOWER_STAGING",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 180
      },
      "locked": false
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-wall-abort-park",
      "name": "wall abort park to RIGHT_PARK",
      "endPoint": {
        "x": 14.5,
        "y": 95.7
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
      },
      "color": "#e15759"
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
        "PickFlower": 3.7,
        "GardenGather": 5.0
      }
    },
    "points": {
      "RIGHT_START": [
        55.6,
        7.7,
        90
      ],
      "WALL_FLOWER_STAGING": [
        23.1,
        47.2,
        180
      ],
      "LEFT_LAUNCH_CLOSE": [
        59.9,
        117.5,
        270
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "RIGHT_PAIR_CLOSE": [
        59.5,
        31,
        97.23932587081833
      ],
      "RIGHT_PARK": [
        14.5,
        95.7,
        0
      ]
    },
    "pathEnds": {
      "r-wall-flower": "WALL_FLOWER_STAGING",
      "r-wall-lane-north": "LEFT_LAUNCH_CLOSE",
      "r-scoring-flower": "SCORING_FLOWER_STAGING",
      "r-loaded-under-hive": "RIGHT_PAIR_CLOSE",
      "r-near-park": "RIGHT_PARK",
      "r-left-abort-park": "RIGHT_PARK",
      "r-flower-abort-park": "RIGHT_PARK",
      "r-wall-abort-park": "RIGHT_PARK"
    },
    "cards": [
      {
        "id": "R-action-3",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "R-action-4",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "card-r-wall-flower",
        "kind": "path",
        "lineId": "r-wall-flower",
        "park": false
      },
      {
        "id": "R-action-5",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 4.5
      },
      {
        "id": "card-r-wall-lane-north",
        "kind": "path",
        "lineId": "r-wall-lane-north",
        "park": false
      },
      {
        "id": "R-wait-8",
        "kind": "firstOf",
        "label": "LEFT up before 14 s, otherwise park",
        "rows": [
          {
            "afterMs": 14000,
            "matchClock": true,
            "cards": [
              {
                "id": "card-r-left-abort-park",
                "kind": "path",
                "lineId": "r-left-abort-park",
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
                "id": "R-action-6",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "card-r-scoring-flower",
                "kind": "path",
                "lineId": "r-scoring-flower",
                "park": false
              },
              {
                "id": "R-action-7",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 4.5
              },
              {
                "id": "card-r-loaded-under-hive",
                "kind": "path",
                "lineId": "r-loaded-under-hive",
                "park": false
              },
              {
                "id": "R-wait-2",
                "kind": "firstOf",
                "label": "RIGHT up before 22.5 s, otherwise park",
                "rows": [
                  {
                    "afterMs": 22500,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "card-r-near-park",
                        "kind": "path",
                        "lineId": "r-near-park",
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
                        "id": "R-action-1",
                        "kind": "action",
                        "name": "LaunchAll",
                        "timeoutS": 0.5
                      },
                      {
                        "id": "card-r-near-park-alt",
                        "kind": "path",
                        "lineId": "r-near-park",
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
    "startAt": "RIGHT_START",
    "exportName": "R-Infraviolet",
    "parkFrom": {
      "card-r-wall-flower": "r-wall-abort-park",
      "card-r-wall-lane-north": "r-left-abort-park",
      "card-r-scoring-flower": "r-flower-abort-park",
      "card-r-loaded-under-hive": "r-near-park"
    }
  }
}
