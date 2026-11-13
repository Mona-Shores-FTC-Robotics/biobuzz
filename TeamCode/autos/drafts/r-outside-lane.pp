{
  "version": "2.0.0",
  "timestamp": "2026-11-11T00:00:00.000Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "headingDeg": 90,
    "locked": false
  },
  "lines": [
    {
      "kind": "atomic",
      "id": "r-wall-flower",
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
      "locked": false,
      "name": "wall flower to WALL_FLOWER_STAGING",
      "color": "#e15759",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "compound",
      "id": "r-wall-lane-north",
      "segments": [
        {
          "kind": "atomic",
          "id": "r-wall-lane-north-up",
          "endPoint": {
            "x": 36,
            "y": 100
          },
          "controlPoints": [
            {
              "x": 27,
              "y": 76
            },
            {
              "x": 32,
              "y": 92
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
                  "endProgress": 1,
                  "interpolationType": "constant",
                  "parameters": {
                    "degrees": 90
                  }
                }
              ]
            }
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "r-wall-lane-north-in",
          "endPoint": {
            "x": 59.9,
            "y": 116.2
          },
          "controlPoints": [
            {
              "x": 40,
              "y": 112
            },
            {
              "x": 52,
              "y": 114
            }
          ],
          "heading": {
            "type": "piecewise",
            "piecewiseHeading": {
              "segments": [
                {
                  "startProgress": 0,
                  "endProgress": 0.5,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 90,
                    "endDeg": 0
                  }
                },
                {
                  "startProgress": 0.5,
                  "endProgress": 1,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 360,
                    "endDeg": 270
                  }
                }
              ]
            }
          },
          "locked": false
        }
      ],
      "name": "wall lane north to LEFT_CLOSE_HIVE",
      "color": "#e15759",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "r-scoring-flower",
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
      "locked": false,
      "name": "scoring flower to SCORING_FLOWER_STAGING",
      "color": "#e15759",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "r-wall-lane-south",
      "endPoint": {
        "x": 45.2,
        "y": 27.9
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 119
        },
        {
          "x": 26,
          "y": 52
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 430
              }
            }
          ]
        }
      },
      "locked": false,
      "name": "wall lane south to RIGHT_CLOSE_NEAR",
      "color": "#e15759",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "r-right-park",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 24,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 70,
        "endDeg": 0
      },
      "locked": false,
      "name": "wall lane park to RIGHT_PARK",
      "color": "#e15759",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "r-link-left",
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
      "kind": "compound",
      "id": "r-left-park",
      "segments": [
        {
          "kind": "atomic",
          "id": "r-left-park-out",
          "endPoint": {
            "x": 38,
            "y": 98.7
          },
          "controlPoints": [
            {
              "x": 56,
              "y": 108
            },
            {
              "x": 47,
              "y": 108
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
                    "degrees": 270
                  }
                },
                {
                  "startProgress": 0.3,
                  "endProgress": 1,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 270,
                    "endDeg": 360
                  }
                }
              ]
            }
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "r-left-park-in",
          "endPoint": {
            "x": 14.5,
            "y": 98.7
          },
          "controlPoints": [],
          "heading": {
            "type": "constant",
            "degrees": 360
          },
          "locked": false
        }
      ],
      "name": "left park to RIGHT_PARK",
      "color": "#e15759",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "r-link-flower",
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
      "locked": false,
      "name": "Link to SCORING_FLOWER_STAGING"
    },
    {
      "kind": "atomic",
      "id": "r-flower-abort-park",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
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
      "locked": false,
      "name": "flower abort park to RIGHT_PARK",
      "color": "#e15759",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "r-link-wall",
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
      "locked": false,
      "name": "Link to WALL_FLOWER_STAGING"
    },
    {
      "kind": "atomic",
      "id": "r-wall-abort-park",
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
      },
      "locked": false,
      "name": "wall abort park to RIGHT_PARK",
      "color": "#e15759",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "r-link-start",
      "endPoint": {
        "x": 55.6,
        "y": 7.7
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 90
      },
      "locked": false,
      "name": "Link to RIGHT_START"
    },
    {
      "kind": "atomic",
      "id": "r-garden",
      "endPoint": {
        "x": 11.5,
        "y": 22.1
      },
      "controlPoints": [
        {
          "x": 46,
          "y": 22
        },
        {
          "x": 30,
          "y": 22.1
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      },
      "locked": false,
      "name": "garden to GARDEN_STAGING",
      "color": "#e15759",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "compound",
      "id": "r-garden-north",
      "segments": [
        {
          "kind": "atomic",
          "id": "r-garden-north-up",
          "endPoint": {
            "x": 36,
            "y": 100
          },
          "controlPoints": [
            {
              "x": 26,
              "y": 30
            },
            {
              "x": 28,
              "y": 76
            }
          ],
          "heading": {
            "type": "piecewise",
            "piecewiseHeading": {
              "segments": [
                {
                  "startProgress": 0,
                  "endProgress": 0.25,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 270,
                    "endDeg": 450
                  }
                },
                {
                  "startProgress": 0.25,
                  "endProgress": 1,
                  "interpolationType": "constant",
                  "parameters": {
                    "degrees": 90
                  }
                }
              ]
            }
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "r-garden-north-in",
          "endPoint": {
            "x": 59.9,
            "y": 116.2
          },
          "controlPoints": [
            {
              "x": 40,
              "y": 112
            },
            {
              "x": 52,
              "y": 114
            }
          ],
          "heading": {
            "type": "piecewise",
            "piecewiseHeading": {
              "segments": [
                {
                  "startProgress": 0,
                  "endProgress": 0.5,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 90,
                    "endDeg": 0
                  }
                },
                {
                  "startProgress": 0.5,
                  "endProgress": 1,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 360,
                    "endDeg": 270
                  }
                }
              ]
            }
          },
          "locked": false
        }
      ],
      "name": "wall lane north from the GARDEN to LEFT_CLOSE_HIVE",
      "color": "#e15759",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": ""
    },
    {
      "kind": "atomic",
      "id": "r-link-garden",
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
      "id": "r-garden-abort-park",
      "segments": [
        {
          "kind": "atomic",
          "id": "r-garden-abort-up",
          "endPoint": {
            "x": 30,
            "y": 80
          },
          "controlPoints": [
            {
              "x": 30,
              "y": 30
            },
            {
              "x": 30,
              "y": 60
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 270,
            "endDeg": 90
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "r-garden-abort-in",
          "endPoint": {
            "x": 14.5,
            "y": 98.7
          },
          "controlPoints": [
            {
              "x": 30,
              "y": 92
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 90,
            "endDeg": 0
          },
          "locked": false
        }
      ],
      "name": "garden abort park to RIGHT_PARK",
      "color": "#e15759",
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
      "LEFT_CLOSE_HIVE": [
        59.9,
        116.2,
        270
      ],
      "RIGHT_PARK": [
        14.5,
        98.7,
        0
      ],
      "GARDEN_STAGING": [
        11.5,
        22.1,
        270
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "RIGHT_CLOSE_NEAR": [
        45.2,
        27.9,
        70
      ]
    },
    "pathEnds": {
      "r-wall-flower": "WALL_FLOWER_STAGING",
      "r-wall-lane-north": "LEFT_CLOSE_HIVE",
      "r-left-park": "RIGHT_PARK",
      "r-wall-abort-park": "RIGHT_PARK",
      "r-garden": "GARDEN_STAGING",
      "r-garden-north": "LEFT_CLOSE_HIVE",
      "r-garden-abort-park": "RIGHT_PARK",
      "r-scoring-flower": "SCORING_FLOWER_STAGING",
      "r-wall-lane-south": "RIGHT_CLOSE_NEAR",
      "r-right-park": "RIGHT_PARK",
      "r-flower-abort-park": "RIGHT_PARK"
    },
    "cards": [
      {
        "id": "R-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "R-launch-preloads",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "R-pickup",
        "kind": "firstOf",
        "label": "Pickup: WALL_FLOWER or GARDEN",
        "choice": "Pickup",
        "rows": [
          {
            "when": [
              "Pickup"
            ],
            "label": "WALL_FLOWER",
            "cards": [
              {
                "id": "card-r-wall-flower",
                "kind": "path",
                "lineId": "r-wall-flower",
                "park": false
              },
              {
                "id": "R-pick-wall",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 4.5
              },
              {
                "id": "card-r-wall-lane-north",
                "kind": "path",
                "lineId": "r-wall-lane-north",
                "park": false
              }
            ]
          },
          {
            "afterMs": 0,
            "label": "GARDEN",
            "cards": [
              {
                "id": "card-r-garden",
                "kind": "path",
                "lineId": "r-garden",
                "park": false
              },
              {
                "id": "R-gather",
                "kind": "action",
                "name": "GardenGather",
                "timeoutS": 5.5
              },
              {
                "id": "card-r-garden-north",
                "kind": "path",
                "lineId": "r-garden-north",
                "park": false
              }
            ]
          }
        ]
      },
      {
        "id": "R-wait-left",
        "kind": "firstOf",
        "label": "LEFT up before 14 s, otherwise park",
        "rows": [
          {
            "afterMs": 14000,
            "matchClock": true,
            "cards": [
              {
                "id": "card-r-left-park-timeout",
                "kind": "path",
                "lineId": "r-left-park",
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
                "id": "R-launch-left",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "R-time-for-third",
                "kind": "firstOf",
                "label": "LEFT volley done before 15.5 s, otherwise park",
                "rows": [
                  {
                    "afterMs": 15500,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "card-r-left-park-late",
                        "kind": "path",
                        "lineId": "r-left-park",
                        "park": true
                      }
                    ]
                  },
                  {
                    "when": [
                      "CellUp"
                    ],
                    "cards": [
                      {
                        "id": "card-r-scoring-flower",
                        "kind": "path",
                        "lineId": "r-scoring-flower",
                        "park": false
                      },
                      {
                        "id": "R-pick-scoring",
                        "kind": "action",
                        "name": "PickFlower",
                        "timeoutS": 4.5
                      },
                      {
                        "id": "card-r-wall-lane-south",
                        "kind": "path",
                        "lineId": "r-wall-lane-south",
                        "park": false
                      },
                      {
                        "id": "R-wait-right",
                        "kind": "firstOf",
                        "label": "RIGHT up before 26 s, otherwise park",
                        "rows": [
                          {
                            "afterMs": 26000,
                            "matchClock": true,
                            "cards": [
                              {
                                "id": "card-r-right-park-timeout",
                                "kind": "path",
                                "lineId": "r-right-park",
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
                                "id": "R-launch-right",
                                "kind": "action",
                                "name": "LaunchAll",
                                "timeoutS": 0.5
                              },
                              {
                                "id": "card-r-right-park",
                                "kind": "path",
                                "lineId": "r-right-park",
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
            ]
          }
        ]
      }
    ],
    "startAt": "RIGHT_START",
    "exportName": "R-OutsideLane",
    "parkFrom": {
      "card-r-wall-flower": "r-wall-abort-park",
      "card-r-wall-lane-north": "r-left-park",
      "card-r-garden": "r-garden-abort-park",
      "card-r-garden-north": "r-left-park",
      "card-r-scoring-flower": "r-flower-abort-park",
      "card-r-wall-lane-south": "r-right-park"
    }
  }
}
