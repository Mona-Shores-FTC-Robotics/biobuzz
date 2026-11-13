{
  "version": "2.0.0",
  "timestamp": "2026-11-13T02:00:41.503Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "locked": false,
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "r-first-pickup",
      "color": "#e15759",
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
      "id": "r-pickup-park",
      "color": "#e15759",
      "name": "pickup park to RIGHT_PARK",
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
      "id": "link-1",
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
      "id": "r-lane-north",
      "color": "#e15759",
      "name": "outside lane to LEFT_WALL_NEAR",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 37.8,
        "y": 130.7
      },
      "controlPoints": [
        {
          "x": 29.5,
          "y": 72
        },
        {
          "x": 29,
          "y": 108
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.4,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            },
            {
              "startProgress": 0.4,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 295.5
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 295.5
              }
            }
          ]
        }
      }
    },
    {
      "id": "r-left-park",
      "color": "#e15759",
      "name": "left park to RIGHT_PARK",
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
          "x": 31,
          "y": 104
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 295.5,
        "endDeg": 360
      }
    },
    {
      "id": "link-2",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 37.8,
        "y": 130.7
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": -64.5
      }
    },
    {
      "id": "r-scoring-flower",
      "color": "#e15759",
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
      "controlPoints": [
        {
          "x": 38,
          "y": 119
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 295.5,
        "endDeg": 450
      }
    },
    {
      "id": "r-flower-park",
      "color": "#e15759",
      "name": "scoring FLOWER park to RIGHT_PARK",
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
          "x": 40,
          "y": 100
        },
        {
          "x": 28,
          "y": 96
        }
      ],
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
      "id": "r-to-catch",
      "color": "#e15759",
      "name": "turn to LEFT_CLOSE_HIVE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.9,
        "y": 116.2
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "r-catch-park",
      "color": "#e15759",
      "name": "catch park to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "compound",
      "segments": [
        {
          "id": "r-catch-park-south",
          "color": "#9c755f",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 50,
            "y": 98.7
          },
          "controlPoints": [
            {
              "x": 57,
              "y": 106
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 270,
            "endDeg": 360
          }
        },
        {
          "id": "r-catch-park-in",
          "color": "#e15759",
          "name": "",
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
          "controlPoints": [],
          "heading": {
            "type": "constant",
            "degrees": 360
          }
        }
      ]
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
      "id": "r-cross",
      "color": "#e15759",
      "name": "back under the HIVE to RIGHT_CLOSE_HIVE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "compound",
      "segments": [
        {
          "id": "r-cross-out",
          "color": "#4e79a7",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 59,
            "y": 97
          },
          "controlPoints": [
            {
              "x": 47.2,
              "y": 108
            },
            {
              "x": 58,
              "y": 108
            }
          ],
          "heading": {
            "type": "constant",
            "degrees": 90
          }
        },
        {
          "id": "r-cross-south",
          "color": "#59a14f",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 56.1,
            "y": 25.3
          },
          "controlPoints": [
            {
              "x": 59.5,
              "y": 70
            },
            {
              "x": 58,
              "y": 45
            }
          ],
          "heading": {
            "type": "constant",
            "degrees": 90
          }
        }
      ]
    },
    {
      "id": "r-right-park",
      "color": "#e15759",
      "name": "outside lane to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "compound",
      "segments": [
        {
          "id": "r-right-park-west",
          "color": "#f28e2b",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 33,
            "y": 34
          },
          "controlPoints": [
            {
              "x": 46,
              "y": 27
            }
          ],
          "heading": {
            "type": "constant",
            "degrees": 90
          }
        },
        {
          "id": "r-right-park-lane",
          "color": "#b07aa1",
          "name": "",
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
              "x": 30,
              "y": 60
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
          }
        }
      ]
    }
  ],
  "shapes": [],
  "sequence": [
    {
      "kind": "path",
      "lineId": "r-first-pickup"
    },
    {
      "kind": "path",
      "lineId": "r-pickup-park"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "r-lane-north"
    },
    {
      "kind": "path",
      "lineId": "r-left-park"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "r-scoring-flower"
    },
    {
      "kind": "path",
      "lineId": "r-flower-park"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "r-to-catch"
    },
    {
      "kind": "path",
      "lineId": "r-catch-park-south"
    },
    {
      "kind": "path",
      "lineId": "r-catch-park-in"
    },
    {
      "kind": "path",
      "lineId": "link-4"
    },
    {
      "kind": "path",
      "lineId": "r-cross-out"
    },
    {
      "kind": "path",
      "lineId": "r-cross-south"
    },
    {
      "kind": "path",
      "lineId": "r-right-park-west"
    },
    {
      "kind": "path",
      "lineId": "r-right-park-lane"
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
        "CameraBlind",
        "Always"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 4,
        "LaunchOne": 0.5,
        "SpinUp": 0.1,
        "PickFlower": 4,
        "GardenGather": 5
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
      "LEFT_WALL_NEAR": [
        37.8,
        130.7,
        295.5
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "LEFT_CLOSE_HIVE": [
        59.9,
        116.2,
        270
      ],
      "RIGHT_CLOSE_HIVE": [
        56.1,
        25.3,
        90
      ],
      "RIGHT_PARK": [
        14.5,
        98.7,
        0
      ]
    },
    "pathEnds": {
      "r-first-pickup": "WALL_FLOWER_STAGING",
      "r-lane-north": "LEFT_WALL_NEAR",
      "r-pickup-park": "RIGHT_PARK",
      "r-left-park": "RIGHT_PARK",
      "r-scoring-flower": "SCORING_FLOWER_STAGING",
      "r-flower-park": "RIGHT_PARK",
      "r-to-catch": "LEFT_CLOSE_HIVE",
      "r-catch-park-in": "RIGHT_PARK",
      "r-cross-south": "RIGHT_CLOSE_HIVE",
      "r-right-park-lane": "RIGHT_PARK"
    },
    "cards": [
      {
        "id": "r-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "r-launch-preloads",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "r-c-pickup",
        "kind": "path",
        "lineId": "r-first-pickup",
        "park": false
      },
      {
        "id": "r-grab-1",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 4.5
      },
      {
        "id": "r-c-lane",
        "kind": "path",
        "lineId": "r-lane-north",
        "park": false
      },
      {
        "id": "r-stop-2",
        "kind": "firstOf",
        "label": "R2: LEFT up by 15.5 s, otherwise park",
        "rows": [
          {
            "afterMs": 15500,
            "matchClock": true,
            "cards": [
              {
                "id": "r-c-left-park",
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
                "id": "r-launch-left",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "r-gate-flower",
                "kind": "firstOf",
                "label": "Time for the scoring FLOWER: before 16.9 s",
                "rows": [
                  {
                    "afterMs": 16900,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "r-c-left-park-s",
                        "kind": "path",
                        "lineId": "r-left-park",
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
                        "id": "r-c-flower",
                        "kind": "path",
                        "lineId": "r-scoring-flower",
                        "park": false
                      },
                      {
                        "id": "r-grab-2",
                        "kind": "action",
                        "name": "PickFlower",
                        "timeoutS": 4.5
                      },
                      {
                        "id": "r-gate-cross",
                        "kind": "firstOf",
                        "label": "Time to cross for TIP 3: before 20.7 s",
                        "rows": [
                          {
                            "afterMs": 20700,
                            "matchClock": true,
                            "cards": [
                              {
                                "id": "r-gate-left",
                                "kind": "firstOf",
                                "label": "Time to finish TIP 2 at LEFT: before 22.4 s",
                                "rows": [
                                  {
                                    "afterMs": 22400,
                                    "matchClock": true,
                                    "cards": [
                                      {
                                        "id": "r-c-flower-park",
                                        "kind": "path",
                                        "lineId": "r-flower-park",
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
                                        "id": "r-c-catch",
                                        "kind": "path",
                                        "lineId": "r-to-catch",
                                        "park": false
                                      },
                                      {
                                        "id": "r-stop-3",
                                        "kind": "firstOf",
                                        "label": "R3: RIGHT up by 23 s, otherwise finish TIP 2 at LEFT",
                                        "rows": [
                                          {
                                            "afterMs": 23000,
                                            "matchClock": true,
                                            "cards": [
                                              {
                                                "id": "r-launch-left-again",
                                                "kind": "action",
                                                "name": "LaunchAll",
                                                "timeoutS": 1
                                              },
                                              {
                                                "id": "r-hold-a",
                                                "kind": "firstOf",
                                                "label": "L has passed: park at 26.4 s",
                                                "rows": [
                                                  {
                                                    "afterMs": 26400,
                                                    "matchClock": true,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "r-c-catch-park-a",
                                                "kind": "path",
                                                "lineId": "r-catch-park",
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
                                                "id": "r-hold-c",
                                                "kind": "firstOf",
                                                "label": "L has passed: park at 26.4 s",
                                                "rows": [
                                                  {
                                                    "afterMs": 26400,
                                                    "matchClock": true,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "r-c-catch-park-c",
                                                "kind": "path",
                                                "lineId": "r-catch-park",
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
                          },
                          {
                            "when": [
                              "Always"
                            ],
                            "cards": [
                              {
                                "id": "r-c-cross",
                                "kind": "path",
                                "lineId": "r-cross",
                                "park": false
                              },
                              {
                                "id": "r-stop-3r",
                                "kind": "firstOf",
                                "label": "R3: RIGHT up by 23.5 s at the right spot, otherwise park",
                                "rows": [
                                  {
                                    "afterMs": 23500,
                                    "matchClock": true,
                                    "cards": [
                                      {
                                        "id": "r-hold-right-no",
                                        "kind": "firstOf",
                                        "label": "L has left for its park: park at 25 s",
                                        "rows": [
                                          {
                                            "afterMs": 25000,
                                            "matchClock": true,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "r-c-right-park-no",
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
                                        "id": "r-launch-right",
                                        "kind": "action",
                                        "name": "LaunchAll",
                                        "timeoutS": 1
                                      },
                                      {
                                        "id": "r-hold-right-yes",
                                        "kind": "firstOf",
                                        "label": "L has left for its park: park at 25 s",
                                        "rows": [
                                          {
                                            "afterMs": 25000,
                                            "matchClock": true,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "r-c-right-park",
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
            ]
          }
        ]
      }
    ],
    "startAt": "RIGHT_START",
    "linkPaths": [
      "link-1",
      "link-2",
      "link-3",
      "link-4"
    ],
    "parkFrom": {
      "r-c-pickup": "r-pickup-park",
      "r-c-lane": "r-left-park",
      "r-c-flower": "r-flower-park",
      "r-c-catch": "r-catch-park",
      "r-c-cross": "r-right-park"
    },
    "exportName": "R-Clockwise-Flower"
  }
}